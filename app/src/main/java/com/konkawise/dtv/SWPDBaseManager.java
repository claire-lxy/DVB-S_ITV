package com.konkawise.dtv;

import android.annotation.NonNull;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.sw.dvblib.SWPDBase;

import java.util.ArrayList;
import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.ChannelNew_t;
import vendor.konka.hardware.dtvmanager.V1_0.Channel_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPEdit_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

public class SWPDBaseManager {
    public static final int RANGE_SAT_INDEX = 10000;

    private static class SWPDBaseManagerHolder {
        private static SWPDBaseManager INSTANCE = new SWPDBaseManager();
    }

    private SWPDBaseManager() {
        SWPDBase.CreateInstance();
    }

    public static SWPDBaseManager getInstance() {
        return SWPDBaseManagerHolder.INSTANCE;
    }

    /**
     * 获取卫星列表，排除第一个T2的信号
     */
    public List<SatInfo_t> getSatList() {
        ArrayList<SatInfo_t> satelliteList = new ArrayList<>();
        int satNum = SWPDBase.CreateInstance().getSatNum(getCurrProgNo());

        for (int i = 1; i < satNum; i++) {
            satelliteList.add(getSatInfo(i));
        }
        return satelliteList;
    }

    /**
     * 根据卫星索引获取某个卫星的信息
     */
    public SatInfo_t getSatInfo(int sat) {
        return SWPDBase.CreateInstance().getSatInfo(sat);
    }

    /**
     * 获取卫星列表，包含T2信号
     */
    public List<SatInfo_t> getProgSatList() {
        return SWPDBase.CreateInstance().getProgSatList();
    }

    /**
     * 根据SatIndex查找所在的卫星位置
     */
    public int findPositionBySatIndex(int satIndex) {
        if (satIndex < 0) return 0;

        List<SatInfo_t> satList = getSatList();
        if (satList != null && !satList.isEmpty()) {
            for (int i = 0; i < satList.size(); i++) {
                if (satList.get(i).SatIndex == satIndex) return i;
            }
        }
        return 0;
    }

    /**
     * 根据卫星索引获取卫星频道列表
     */
    public List<ChannelNew_t> getSatChannelInfoList(int satIndex) {
        return SWPDBase.CreateInstance().getSatChannelInfo(satIndex);
    }

    /**
     * 根据卫星索引获取单个卫星频道信息
     *
     * @param sat   卫星索引
     * @param index 循环索引 SWPDBase.getSatChannelNum()获取的数量索引
     */
    public ChannelNew_t getChannelInfoBySat(int sat, int index) {
        return SWPDBase.CreateInstance().getChannelInfoBySat(sat, index);
    }

    /**
     * 添加TP频点
     */
    public void addChannelInfo(Channel_t tp) {
        SWPDBase.CreateInstance().addChannelInfo(tp);
    }

    /**
     * 删除TP频点
     */
    public void delChannelInfo(ChannelNew_t tp) {
        SWPDBase.CreateInstance().delChannelInfo(tp.ChannelIndex);
    }

    /**
     * 编辑TP频点
     */
    public void setSatChannelInfo(ChannelNew_t tp) {
        SWPDBase.CreateInstance().setSatChannelInfo(tp);
    }

    /**
     * 获取所有频道列表
     */
    public List<PDPMInfo_t> getTotalGroupProgList() {
        return getTotalGroupProgList(new int[1]);
    }

    /**
     * 获取Total分组下对应的卫星频道列表
     *
     * @param ltTotalProgs
     * @param satIndex
     * @return
     */
    public List<PDPMInfo_t> getTotalGroupSatProgList(List<PDPMInfo_t> ltTotalProgs, int satIndex) {
        List<PDPMInfo_t> ltSatProgs = new ArrayList<>();
        if (ltTotalProgs == null || ltTotalProgs.size() == 0) {
            return ltSatProgs;
        }
        if(satIndex == -1){
            return ltTotalProgs;
        }
        for (PDPMInfo_t progInfo : ltTotalProgs) {
            if (progInfo.Sat == satIndex) {
                ltSatProgs.add(progInfo);
            }
        }
        return ltSatProgs;
    }

    public List<PDPMInfo_t> getTotalGroupProgList(int[] index) {
        return getGroupProgList(SWPDBase.SW_TOTAL_GROUP, index);
    }

    /**
     * 获取所有频道列表，不包含skip频道
     */
    public List<PDPMInfo_t> getWholeGroupProgList() {
        return getWholeGroupProgList(new int[1]);
    }

    public List<PDPMInfo_t> getWholeGroupProgList(int[] index) {
        return getGroupProgList(SWPDBase.SW_WHOLE_GROUP, index);
    }

    private List<PDPMInfo_t> getGroupProgList(int group, int[] index) {
        setCurrGroup(group, 1);
        return getCurrGroupProgList(index);
    }

    public ArrayList<PDPMInfo_t> getCurrGroupProgList(int[] currProgNumArray) {
        return SWPDBase.CreateInstance().getCurrGroupProgList(currProgNumArray);
    }

    public List<PDPInfo_t> getCurrGroupProgInfoList() {
        List<PDPInfo_t> progInfoList = new ArrayList<>();
        List<PDPMInfo_t> wholeGroupProgList = getWholeGroupProgList();
        if (wholeGroupProgList != null && !wholeGroupProgList.isEmpty()) {
            for (PDPMInfo_t progInfo : wholeGroupProgList) {
                PDPInfo_t info = new PDPInfo_t();
                info.Sat = progInfo.Sat;
                info.Freq = progInfo.Freq;
                info.TsID = progInfo.TsID;
                info.ServID = progInfo.ServID;
                info.ServType = progInfo.ServType;
                info.Name = progInfo.Name;
                progInfoList.add(info);
            }
        }
        return progInfoList;
    }

    public List<PDPInfo_t> getAnotherTypeProgInfoList() {
        int currProgType = SWPDBaseManager.getInstance().getCurrProgType();
        setCurrProgType(currProgType == SWPDBase.SW_GBPROG ? SWPDBase.SW_TVPROG : SWPDBase.SW_GBPROG, 0);
        List<PDPInfo_t> progInfoList = getCurrGroupProgInfoList();
        setCurrProgType(currProgType, 0);

        return progInfoList;
    }

    /**
     * 编辑喜爱分组
     *
     * @param favType     喜爱分组索引 SWPDBase.SWFAV0~SWFAV7
     * @param favlist     添加到喜爱分组的频道索引号列表
     * @param favlistsize 添加到喜爱分组的频道数量
     * @param store       是否保存 store=1表示保存
     */
    public void editFavProgList(int favType, int[] favlist, int favlistsize, int store) {
        SWPDBase.CreateInstance().editFavProgList(favType, favlist, favlistsize, store);
    }

    /**
     * 保存频道编辑，喜爱分组
     */
    public void saveFavorite(SparseArray<List<PDPMInfo_t>> mFavoriteChannelsMap) {
        for (int i = 0; i < mFavoriteChannelsMap.size(); i++) {
            List<PDPMInfo_t> editFavoriteList = mFavoriteChannelsMap.get(i);
            if (editFavoriteList == null) continue;

            mFavoriteChannelsMap.put(i, editFavoriteList);

            SWPDBaseManager.getInstance().editFavProgList(i,
                    getFavoriteProgIndexs(mFavoriteChannelsMap.get(i)), mFavoriteChannelsMap.get(i).size(), 1);
        }
    }

    private int[] getFavoriteProgIndexs(List<PDPMInfo_t> favoriteChannelList) {
        int[] favoriteProgIndexs = new int[favoriteChannelList.size()];
        for (int i = 0; i < favoriteChannelList.size(); i++) {
            favoriteProgIndexs[i] = favoriteChannelList.get(i).ProgIndex;
        }
        return favoriteProgIndexs;
    }

    /**
     * 保存频道编辑，lock、skip、rename、delete
     */
    public void editGroupProgList(ArrayList<PDPEdit_t> list) {
        SWPDBase.CreateInstance().editGroupProgList(list);
    }

    /**
     * 获取排序方式
     */
    public int getSortType() {
        return SWPDBase.CreateInstance().getSortType();
    }

    /**
     * 设置排序方式
     *
     * @param sortType sortType=1 A to Z
     *                 sortType=2 Z to A
     *                 sortType=3 CAS
     */
    public void setSortType(int sortType) {
        SWPDBase.CreateInstance().setSortType(sortType);
    }

    /**
     * 获取频道PID
     *
     * @param index 频道索引号
     */
    public int[] getServicePID(int index) {
        return SWPDBase.CreateInstance().getServicePID(index);
    }

    /**
     * 设置频道PID
     *
     * @param index 频道索引号
     */
    public void setServicePID(int index, int vid, int aid, int pcrid) {
        SWPDBase.CreateInstance().setServicePID(index, vid, aid, pcrid);
    }

    /**
     * 保存卫星信息
     *
     * @param Sat     卫星索引
     * @param satinfo 保存的卫星信息
     */
    public void setSatInfo(int Sat, SatInfo_t satinfo) {
        SWPDBase.CreateInstance().setSatInfo(Sat, satinfo);
    }

    /**
     * 获取当前频道信息
     */
    public PDPMInfo_t getCurrProgInfo() {
        return SWPDBase.CreateInstance().getCurrProgMangInfo();
    }

    /**
     * 设置当前频道类型
     *
     * @param type type=0代表TV类型，type=1代表RADIO类型
     */
    public void setCurrProgType(int type, int param) {
        SWPDBase.CreateInstance().setCurrProgType(type, param);
    }

    /**
     * 设置当前频道播放号
     */
    public void setCurrProgNo(int index) {
        SWPDBase.CreateInstance().setCurrProgNo(index);
    }

    /**
     * 总频道数
     */
    public int getProgNumOfCurrGroup() {
        return SWPDBase.CreateInstance().getProgNumOfCurrGroup();
    }

    /**
     * 获取当前频道类型
     */
    public int getCurrProgType() {
        return SWPDBase.CreateInstance().getCurrProgType();
    }

    public void setCurrGroup(int group, int groupid) {
        SWPDBase.CreateInstance().setCurrGroup(group, groupid);
    }

    public int getCurrGroup() {
        return SWPDBase.CreateInstance().getCurrGroup();
    }

    public int getCurrGroupParam() {
        return SWPDBase.CreateInstance().getCurrGroupParam();
    }

    /**
     * 获取当前频道播放号
     */
    public int getCurrProgNo() {
        return SWPDBase.CreateInstance().getCurrProgNo();
    }

    /**
     * 根据serviceId、tsid、sat获取对应的频道
     */
    public PDPInfo_t getProgInfoByServiceId(int serviceid, int tsid, int sat) {
        PDPInfo_t progInfo = SWPDBase.CreateInstance().getProgInfoOfServiceID(serviceid, tsid, sat);
        if (progInfo != null) {
            progInfo.TsID = progInfo.Freq; // 底层获取到的tsid是对应在Freq，手动修改一次
        }
        return progInfo;
    }

    /**
     * 获取卫星列表，包含自定义的ALL
     */
    public List<SatInfo_t> getAllSatList(Context context) {
        List<SatInfo_t> satList = getProgSatList();
        if (satList != null) {
            SatInfo_t allSatInfo = new SatInfo_t();
            allSatInfo.SatIndex = -1;
            allSatInfo.sat_name = context.getString(R.string.all);
            satList.add(0, allSatInfo);
            return satList;
        }
        return null;
    }

    /**
     * 获取卫星列表，如果有喜爱频道还包含喜爱频道列表
     */
    public List<SatInfo_t> getAllSatListContainFav(Context context) {
        List<SatInfo_t> allSatList = getAllSatList(context);
        if (allSatList != null && !allSatList.isEmpty()) {
            int[] favIndexArray = getFavIndexArray();
            for (int favIndex : favIndexArray) {
                int favProgNum = getProgNumOfGroup(SWPDBase.SW_FAV_GROUP, favIndex);
                if (favProgNum > 0) {
                    SatInfo_t favSatInfo = new SatInfo_t();
                    favSatInfo.SatIndex = favIndex + RANGE_SAT_INDEX; // 存入favIndex，方便切换时获取对应的喜爱分组频道列表展示，加上一个大数值与其他SatIndex区分
                    favSatInfo.sat_name = getFavoriteGroupNameByIndex(favIndex);
                    allSatList.add(favSatInfo);
                }
            }
            return allSatList;
        }
        return null;
    }

    /**
     * 根据group获取对应的列表数量（获取radio列表不正确）
     *
     * @param group SWPDBase.SW_XXX
     * @param param 所在分组，如果没有传0
     */
    public int getProgNumOfGroup(int group, int param) {
        return SWPDBase.CreateInstance().getProgNumOfGroup(group, param);
    }

    /**
     * 根据group获取对应的列表数量
     *
     * @param type  SWPDBase.SW_XXX
     * @param param 所在分组，如果没有传0
     */
    public int getProgNumOfType(int type, int param) {
        return SWPDBase.CreateInstance().getProgNumOfType(type, param);
    }

    /**
     * 通过已知的频道列表，解析出喜爱分组列表，并使用缓存，只需要从底层拿一次数据就可以
     * @param ltChannels
     * @return
     */
    public SparseArray<List<PDPMInfo_t>> getFavChannelMap(List<PDPMInfo_t> ltChannels) {
        SparseArray<List<PDPMInfo_t>> mFavChannelsMap = new SparseArray<>();

        if (ltChannels == null || ltChannels.size() == 0) {
            ltChannels = getTotalGroupProgList();
        }

        int[] favIndexArray = getFavIndexArray();
        for (int i = 0; i < favIndexArray.length; i++) {
            List<PDPMInfo_t> ltGroupInfos = new ArrayList<>();
            for (int j = 0; j < ltChannels.size(); j++) {
                if ((ltChannels.get(j).FavFlag & (0x0001 << i)) >> i == 1) {
                    ltGroupInfos.add(ltChannels.get(j));
                }
            }
            mFavChannelsMap.put(i, ltGroupInfos);
        }
        return mFavChannelsMap;
    }

    /**
     * 获取喜爱分组索引列表
     */
    public int[] getFavIndexArray() {
        int[] favIndexArray = new int[10];
        favIndexArray[0] = SWPDBase.SW_FAV0;
        favIndexArray[1] = SWPDBase.SW_FAV1;
        favIndexArray[2] = SWPDBase.SW_FAV2;
        favIndexArray[3] = SWPDBase.SW_FAV3;
        favIndexArray[4] = SWPDBase.SW_FAV4;
        favIndexArray[5] = SWPDBase.SW_FAV5;
        favIndexArray[6] = SWPDBase.SW_FAV6;
        favIndexArray[7] = SWPDBase.SW_FAV7;
        favIndexArray[8] = SWPDBase.SW_FAV8;
        favIndexArray[9] = SWPDBase.SW_FAV9;
        return favIndexArray;
    }

    /**
     * 根据喜爱分组索引获取对应的喜爱分组列表
     *
     * @param favIndex SWPDBase.SW_FAV0~7
     */
    public List<PDPMInfo_t> getFavListByIndex(int favIndex) {
        return getCurrGroupProgListByCond(2, favIndex);
    }

    /**
     * 获取频道列表
     *
     * @param condType  condType=1所在卫星的频道列表 condType=2喜爱分组列表
     * @param condindex condType=1则condindex应传入卫星索引，condType=2则condindex=SWPDBase.SW_FAV0~7
     */
    public List<PDPMInfo_t> getCurrGroupProgListByCond(int condType, int condindex) {
        return SWPDBase.CreateInstance().getCurrGroupProgListByCond(new int[1], condType, condindex);
    }

    /**
     * 获取喜爱分组名称列表
     */
    public List<String> getFavoriteGroupNameList(int size) {
        List<String> favoriteGroupNameList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            favoriteGroupNameList.add(getFavoriteGroupNameByIndex(i));
        }
        return favoriteGroupNameList;
    }

    /**
     * 根据喜爱分组索引获取对应喜爱分组名称
     *
     * @param favIndex SWPDBase.SW_FAV0~7
     */
    private String getFavoriteGroupNameByIndex(int favIndex) {
        String favoriteGroupName = SWPDBase.CreateInstance().getFavName(favIndex);
        return TextUtils.isEmpty(favoriteGroupName) ? "FAV" + favIndex : favoriteGroupName;
    }

    public void setFavGroupName(int favGroupIndex, String favGroupName) {
        SWPDBase.CreateInstance().setFavName(favGroupIndex, favGroupName);
    }

    public boolean isProgCanPlay() {
        return getCurrProgInfo() != null;
    }

    /**
     * 根据频道FavFlag标志获取频道所在喜爱分组数组
     *
     * @return ['0', '0', '0', '0', '0', '0', '0', '1'] 如果频道在某个喜爱分组中为1，否则为0
     */
    public char[] getProgInfoFavGroupArray(@NonNull PDPMInfo_t progInfo) {
        int[] favIndexArray = getFavIndexArray();
        char[] favGroupArray = new char[favIndexArray.length];

        StringBuilder sb = new StringBuilder(favIndexArray.length);
        String favFlagBinaryStr = Integer.toBinaryString(progInfo.FavFlag);
        favFlagBinaryStr = sb.append(favFlagBinaryStr).reverse().toString();
        for (int i = 0; i < favIndexArray.length; i++) {
            if (favFlagBinaryStr.length() - 1 < i) {
                favGroupArray[i] = '0';
            } else {
                favGroupArray[i] = favFlagBinaryStr.charAt(i);
            }
        }
        return favGroupArray;
    }
}
