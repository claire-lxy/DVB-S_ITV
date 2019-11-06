package com.konkawise.dtv;

import android.annotation.NonNull;
import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;

import com.sw.dvblib.DTVProg;

import java.util.ArrayList;
import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_TP;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Enum_Group;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Enum_Type;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgEditInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgBasicInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_SatInfo;

public class DTVProgramManager {
    public static final int RANGE_SAT_INDEX = 10000;

    private static class DTVProgramManagerHolder {
        private static DTVProgramManager INSTANCE = new DTVProgramManager();
    }

    private DTVProgramManager() {
        DTVProg.getInstance();
    }

    public static DTVProgramManager getInstance() {
        return DTVProgramManagerHolder.INSTANCE;
    }

    /**
     * 获取卫星列表，只获取S2
     */
    public List<HProg_Struct_SatInfo> getSatList() {
        return getSatList(true);
    }

    public List<HProg_Struct_SatInfo> getSatList(boolean onlyS) {
        ArrayList<HProg_Struct_SatInfo> satelliteList = new ArrayList<>();
        int satNum = DTVProg.getInstance().getSatNum(getCurrProgNo()) + Constants.SatIndex.EXCLUDE_SAT_NUM;
        int startIndex = onlyS ? Constants.SatIndex.S_START_INDEX : Constants.SatIndex.ALL_START_INDEX;
        for (int i = startIndex; i < satNum; i++) {
            HProg_Struct_SatInfo satInfo = getSatInfo(i);
            if (satInfo != null) {
                satelliteList.add(satInfo);
            }
        }
        return satelliteList;
    }

    /**
     * 根据卫星索引获取某个卫星的信息
     */
    public HProg_Struct_SatInfo getSatInfo(int sat) {
        return DTVProg.getInstance().getSatInfo(sat);
    }

    /**
     * 获取所有卫星列表，T、C、S全部获取
     */
    public List<HProg_Struct_SatInfo> getSatInfoList() {
        return DTVProg.getInstance().getSatInfoList();
    }

    /**
     * 根据SatIndex查找所在的卫星位置
     */
    public int findPositionBySatIndex(int satIndex) {
        if (satIndex < 0) return 0;

        List<HProg_Struct_SatInfo> satList = getSatList();
        if (satList != null && !satList.isEmpty()) {
            for (int i = 0; i < satList.size(); i++) {
                if (satList.get(i).SatIndex == satIndex) return i;
            }
        }
        return 0;
    }

    public boolean isSatGroup(int currGroup, int currGroupParam, HProg_Struct_SatInfo satInfo) {
        return currGroup == HProg_Enum_Group.SAT_GROUP && currGroupParam == satInfo.SatIndex;
    }

    public boolean isFavGroup(int currGroup, int currGroupParam, HProg_Struct_SatInfo satInfo) {
        return currGroup == HProg_Enum_Group.FAV_GROUP && currGroupParam == (satInfo.SatIndex - RANGE_SAT_INDEX);
    }

    /**
     * 根据卫星索引获取卫星频道列表
     */
    public List<HProg_Struct_TP> getSatTPInfo(int satIndex) {
        return DTVProg.getInstance().getSatTPInfo(satIndex);
    }

    /**
     * 根据卫星索引获取单个卫星频道信息
     *
     * @param sat   卫星索引
     * @param index 循环索引 SWPDBase.getSatChannelNum()获取的数量索引
     */
    public HProg_Struct_TP getTPInfoBySat(int sat, int index) {
        return DTVProg.getInstance().getTPInfoBySat(sat, index);
    }

    /**
     * 添加TP频点
     */
    public void addTPInfo(HProg_Struct_TP tp) {
        DTVProg.getInstance().addTPInfo(tp);
    }

    /**
     * 删除TP频点
     */
    public void delTPInfo(HProg_Struct_TP tp) {
        DTVProg.getInstance().delTPInfo(tp.TPIndex);
    }

    /**
     * 编辑TP频点
     */
    public void setTPInfo(HProg_Struct_TP tp) {
        DTVProg.getInstance().setTPInfo(tp.TPIndex, tp);
    }

    /**
     * 获取所有频道列表
     */
    public List<HProg_Struct_ProgInfo> getTotalGroupProgInfoList() {
        return getTotalGroupProgInfoList(new int[1]);
    }

    /**
     * 获取Total分组下对应的卫星频道列表
     */
    public List<HProg_Struct_ProgInfo> getTotalGroupSatProgList(List<HProg_Struct_ProgInfo> ltTotalProgs, int satIndex) {
        List<HProg_Struct_ProgInfo> ltSatProgs = new ArrayList<>();
        if (ltTotalProgs == null || ltTotalProgs.size() == 0) {
            return ltSatProgs;
        }
        if(satIndex == Constants.SatIndex.ALL_SAT_INDEX){
            return ltTotalProgs;
        }
        for (HProg_Struct_ProgInfo progInfo : ltTotalProgs) {
            if (progInfo.Sat == satIndex) {
                ltSatProgs.add(progInfo);
            }
        }
        return ltSatProgs;
    }

    public List<HProg_Struct_ProgInfo> getTotalGroupProgInfoList(int[] index) {
        return getCurrGroupProgInfoList(HProg_Enum_Group.TOTAL_GROUP, index);
    }

    /**
     * 获取所有频道列表，不包含skip频道
     */
    public List<HProg_Struct_ProgInfo> getWholeGroupProgInfoList() {
        return getWholeGroupProgInfoList(new int[1]);
    }

    public List<HProg_Struct_ProgInfo> getWholeGroupProgInfoList(int[] index) {
        return getCurrGroupProgInfoList(HProg_Enum_Group.WHOLE_GROUP, index);
    }

    private List<HProg_Struct_ProgInfo> getCurrGroupProgInfoList(int group, int[] index) {
        setCurrGroup(group, 1);
        return getCurrGroupProgInfoList(index);
    }

    public ArrayList<HProg_Struct_ProgInfo> getCurrGroupProgInfoList(int[] currProgNumArray) {
        return DTVProg.getInstance().getCurrGroupProgInfoList(currProgNumArray);
    }

    public List<HProg_Struct_ProgBasicInfo> getCurrGroupProgInfoList() {
        List<HProg_Struct_ProgBasicInfo> progInfoList = new ArrayList<>();
        List<HProg_Struct_ProgInfo> wholeGroupProgList = getWholeGroupProgInfoList();
        if (wholeGroupProgList != null && !wholeGroupProgList.isEmpty()) {
            for (HProg_Struct_ProgInfo progInfo : wholeGroupProgList) {
                HProg_Struct_ProgBasicInfo info = new HProg_Struct_ProgBasicInfo();
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

    public List<HProg_Struct_ProgBasicInfo> getAnotherTypeProgInfoList() {
        int currProgType = DTVProgramManager.getInstance().getCurrProgType();
        setCurrProgType(currProgType == HProg_Enum_Type.GBPROG ? HProg_Enum_Type.TVPROG : HProg_Enum_Type.GBPROG, 0);
        List<HProg_Struct_ProgBasicInfo> progInfoList = getCurrGroupProgInfoList();
        setCurrProgType(currProgType, 0);

        return progInfoList;
    }

    /**
     * 编辑喜爱分组
     *
     * @param favType     喜爱分组索引 SWPDBase.SWFAV0~SWFAV7
     * @param favlist     添加到喜爱分组的频道索引号列表
     * @param store       是否保存 store=1表示保存
     */
    public void editFavProgList(int favType, int[] favlist, int store) {
        DTVProg.getInstance().editFavProgList(favType, favlist, store);
    }

    /**
     * 保存频道编辑，喜爱分组
     */
    public void saveFavorite(SparseArray<List<HProg_Struct_ProgInfo>> mFavoriteChannelsMap) {
        for (int i = 0; i < mFavoriteChannelsMap.size(); i++) {
            List<HProg_Struct_ProgInfo> editFavoriteList = mFavoriteChannelsMap.get(i);
            if (editFavoriteList == null) continue;

            mFavoriteChannelsMap.put(i, editFavoriteList);

            DTVProgramManager.getInstance().editFavProgList(i, getFavoriteProgIndexs(mFavoriteChannelsMap.get(i)), 1);
        }
    }

    private int[] getFavoriteProgIndexs(List<HProg_Struct_ProgInfo> favoriteChannelList) {
        int[] favoriteProgIndexs = new int[favoriteChannelList.size()];
        for (int i = 0; i < favoriteChannelList.size(); i++) {
            favoriteProgIndexs[i] = favoriteChannelList.get(i).ProgIndex;
        }
        return favoriteProgIndexs;
    }

    /**
     * 保存频道编辑，lock、skip、rename、delete
     */
    public void editGroupProgList(ArrayList<HProg_Struct_ProgEditInfo> list) {
        DTVProg.getInstance().editGroupProgList(list);
    }

    /**
     * 获取排序方式
     */
    public int getSortType() {
        return DTVProg.getInstance().getSortType();
    }

    /**
     * 设置排序方式
     *
     * @param sortType sortType=1 DISEQC_A to Z
     *                 sortType=2 Z to DISEQC_A
     *                 sortType=3 CAS
     */
    public void setSortType(int sortType) {
        DTVProg.getInstance().setSortType(sortType);
    }

    /**
     * 获取频道PID
     *
     * @param index 频道索引号
     */
    public int[] getServicePID(int index) {
        return DTVProg.getInstance().getServicePID(index);
    }

    /**
     * 设置频道PID
     *
     * @param index 频道索引号
     */
    public void setServicePID(int index, int vid, int aid, int pcrid) {
        DTVProg.getInstance().setServicePID(index, vid, aid, pcrid);
    }

    /**
     * 保存卫星信息
     *
     * @param Sat     卫星索引
     * @param satinfo 保存的卫星信息
     */
    public void setSatInfo(int Sat, HProg_Struct_SatInfo satinfo) {
        DTVProg.getInstance().setSatInfo(Sat, satinfo);
    }

    /**
     * 获取当前频道信息
     */
    public HProg_Struct_ProgInfo getCurrProgInfo() {
        return DTVProg.getInstance().getCurrProgMangInfo();
    }

    /**
     * 设置当前频道类型
     *
     * @param type type=0代表TV类型，type=1代表RADIO类型
     */
    public void setCurrProgType(int type, int param) {
        DTVProg.getInstance().setCurrProgType(type, param);
    }

    /**
     * 设置当前频道播放号
     */
    public void setCurrProgNo(int index) {
        DTVProg.getInstance().setCurrProgNo(index);
    }

    /**
     * 总频道数
     */
    public int getProgNumOfCurrGroup() {
        return DTVProg.getInstance().getProgNumOfCurrGroup();
    }

    /**
     * 获取当前频道类型
     */
    public int getCurrProgType() {
        return DTVProg.getInstance().getCurrProgType();
    }

    public void setCurrGroup(int group, int groupid) {
        DTVProg.getInstance().setCurrGroup(group, groupid);
    }

    public int getCurrGroup() {
        return DTVProg.getInstance().getCurrGroup();
    }

    public int getCurrGroupParam() {
        return DTVProg.getInstance().getCurrGroupParam();
    }

    /**
     * 获取当前频道播放号
     */
    public int getCurrProgNo() {
        return DTVProg.getInstance().getCurrProgNo();
    }

    /**
     * 根据serviceId、tsid、sat获取对应的频道
     */
    public HProg_Struct_ProgBasicInfo getProgInfoByServiceId(int serviceid, int tsid, int sat) {
        HProg_Struct_ProgBasicInfo progInfo = DTVProg.getInstance().getProgInfoOfServiceID(serviceid, tsid, sat);
        if (progInfo != null) {
            progInfo.TsID = progInfo.Freq; // 底层获取到的tsid是对应在Freq，手动修改一次
        }
        return progInfo;
    }

    /**
     * 获取卫星列表，包含自定义的ALL
     */
    public List<HProg_Struct_SatInfo> getAllSatList(Context context) {
        List<HProg_Struct_SatInfo> satList = getSatInfoList();
        if (satList != null) {
            HProg_Struct_SatInfo allSatInfo = new HProg_Struct_SatInfo();
            allSatInfo.SatIndex = Constants.SatIndex.ALL_SAT_INDEX;
            allSatInfo.sat_name = context.getString(R.string.all);
            satList.add(0, allSatInfo);
            return satList;
        }
        return null;
    }

    /**
     * 获取卫星列表，如果有喜爱频道还包含喜爱频道列表
     */
    public List<HProg_Struct_SatInfo> getAllSatListContainFav(Context context) {
        List<HProg_Struct_SatInfo> allSatList = getAllSatList(context);
        if (allSatList != null && !allSatList.isEmpty()) {
            int[] favIndexArray = getFavIndexArray();
            for (int favIndex : favIndexArray) {
                int favProgNum = getProgNumOfGroup(HProg_Enum_Group.FAV_GROUP, favIndex);
                if (favProgNum > 0) {
                    HProg_Struct_SatInfo favSatInfo = new HProg_Struct_SatInfo();
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
        return DTVProg.getInstance().getProgNumOfGroup(group, param);
    }

    /**
     * 根据group获取对应的列表数量
     *
     * @param type  SWPDBase.SW_XXX
     * @param param 所在分组，如果没有传0
     */
    public int getProgNumOfType(int type, int param) {
        return DTVProg.getInstance().getProgNumOfType(type, param);
    }

    /**
     * 通过已知的频道列表，解析出喜爱分组列表，并使用缓存，只需要从底层拿一次数据就可以
     */
    public SparseArray<List<HProg_Struct_ProgInfo>> getFavChannelMap(List<HProg_Struct_ProgInfo> ltChannels) {
        SparseArray<List<HProg_Struct_ProgInfo>> mFavChannelsMap = new SparseArray<>();

        if (ltChannels == null || ltChannels.size() == 0) {
            ltChannels = getTotalGroupProgInfoList();
        }

        int[] favIndexArray = getFavIndexArray();
        for (int i = 0; i < favIndexArray.length; i++) {
            List<HProg_Struct_ProgInfo> ltGroupInfos = new ArrayList<>();
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
        for (int i = 0; i < 10; i++) {
            favIndexArray[i] = i;
        }
        return favIndexArray;
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
        String favoriteGroupName = DTVProg.getInstance().getFavName(favIndex);
        return TextUtils.isEmpty(favoriteGroupName) ? "FAV" + favIndex : favoriteGroupName;
    }

    public void setFavGroupName(int favGroupIndex, String favGroupName) {
        DTVProg.getInstance().setFavName(favGroupIndex, favGroupName);
    }

    public boolean isProgCanPlay() {
        return getCurrProgInfo() != null;
    }

    /**
     * 根据频道FavFlag标志获取频道所在喜爱分组数组
     *
     * @return ['0', '0', '0', '0', '0', '0', '0', '1'] 如果频道在某个喜爱分组中为1，否则为0
     */
    public char[] getProgInfoFavGroupArray(@NonNull  HProg_Struct_ProgInfo progInfo) {
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
