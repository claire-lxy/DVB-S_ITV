# 项目简介

- iTV：DVB节目播放、EPG、卫星节目扫描、Booking、DTV Setting等。

- 原仓库地址： `git@git.konkawise.com:apk/androidapp.git`
  原仓库路径： `apps/KonkaDvbSetting`
  原仓库分支： `dev:c2fe8bbddebaa28f40b3725c5fe3c3270d9b22cf`

# iTV目录简介

<<<<<<< HEAD
| 目录 | 介绍 |
|:--:|:--:|
| base | 存放四大组件、Dialog等基类，基类一般都是抽象类提供继承 |
| adapter | 存放列表适配器，ListView和RecyclerView都需要继承adapter包内部base包下的适配器基类 |
| annotation | 存放注解类，一般在该包下新建注解作为枚举（使用annotation的方式作为枚举能够提高程序运行效率） |
| bean | 存放ui层本地数据类 |
| dialog | 存放对话框，所有对话框没有特殊情况，统一继承base包下提供的BaseDialogFragment |
| fragment | 存放Fragment，Fragment应该继承base包下的基类 |
| receiver | 存放BroadcastReceiver |
| service | 存放Service，Service应该继承base包下的BaseService兼容Android版本 |
| sp | 存放SharedPreference，内部提供SharedPreference支持存放数据的策略类和实现。具体调用使用PreferenceManager单例管理存储和获取数据 |
| ui | 存放Activity，Activity应该继承base包下的基类BaseActivity |
| utils | 存放工具类 |
| view | 存放自定义的widget |
| weektool | 存放弱引用工具类。目前提供了TimerTask、AsyncTask、Handler、Runnable几种弱引用工具类。在其他类中使用弱引用工具类前，确保类已经实现了WeakToolInterface，在类销毁前，通过WeakTookManager单例管理移除引用，防止内存泄漏。base包下的基类已经集成了弱引用工具管理 |

# iTV管理类简介

| 类名 | 功能 |
|:--:|:--:|
| Constants | 常量类，存放全局常量以及Intent、SharedPreference相关的key |
=======
|    目录    |                             介绍                             |
| :--------: | :----------------------------------------------------------: |
|    base    |    存放四大组件、Dialog等基类，基类一般都是抽象类提供继承    |
|  adapter   | 存放列表适配器，ListView和RecyclerView都需要继承adapter包内部base包下的适配器基类 |
| annotation | 存放注解类，一般在该包下新建注解作为枚举（使用annotation的方式作为枚举能够提高程序运行效率） |
|    bean    |                      存放ui层本地数据类                      |
|   dialog   | 存放对话框，所有对话框没有特殊情况，统一继承base包下提供的BaseDialogFragment |
|  fragment  |         存放Fragment，Fragment应该继承base包下的基类         |
|  receiver  |                    存放BroadcastReceiver                     |
|  service   | 存放Service，Service应该继承base包下的BaseService兼容Android版本 |
|     sp     | 存放SharedPreference，内部提供SharedPreference支持存放数据的策略类和实现。具体调用使用PreferenceManager单例管理存储和获取数据 |
|     ui     |   存放Activity，Activity应该继承base包下的基类BaseActivity   |
|   utils    |                          存放工具类                          |
|    view    |                      存放自定义的widget                      |
|  weektool  | 存放弱引用工具类。目前提供了TimerTask、AsyncTask、Handler、Runnable几种弱引用工具类。在其他类中使用弱引用工具类前，确保类已经实现了WeakToolInterface，在类销毁前，通过WeakTookManager单例管理移除引用，防止内存泄漏。base包下的基类已经集成了弱引用工具管理 |

# iTV管理类简介

|   类名    |                           功能                            |
| :-------: | :-------------------------------------------------------: |
| Constants | 常量类，存放全局常量以及Intent、SharedPreference相关的key |

>>>>>>> ed6f2beeadf095445d266fbfcf7d05c4306fd0cc
| HandlerMsgManager | Handler消息管理类，Handler的sendMessage和removeMessage都应该使用该管理类处理，消息的数据封装使用bean包下的HandlerMsgModel设置消息参数
| LanguageManager | 语言管理类，默认跟随系统语言修改apk语言 |
| PreferenceManager | SharedPreference管理类 |
| SWxxxManager | framework底层接口管理类 |
| ThreadPoolManager | 线程池管理类，线程池运行的Runnable线程应该使用弱引用工具包中的WeakRunnable，防止内存泄漏 |
| UsbManager | 监听Usb插拔管理类 |
| WeakToolManager | 弱引用管理工具类，使用weaktool包下的工具类前，为了能得到WeakToolManager类的管理，类应该实现weakTool包下的WeakToolInterface，在类销毁前使用WeakToolManager.removeWeakTool()移除弱引用工具类，防止内存泄漏 |
| RealTimeManager | 获取实时时间管理工具类，通过注册hidl发送的消息获取实时时间 |

# iTV引用jar包

- swdvb.jar：集成hidl相关的底层接口，ui层通过SWxxxManager单例管理类统一调用

# Telesystem项目中调试运行apk过程

- 在Android Studio中 `Build->Build Bundle(s)/APK(s)->Build APK(s)`，在 `app/build/outputs/apk/debug` 生成iTV.apk

- 将iTV.apk复制到Android 9源码项目 `device/hisilicon/konka/packages/apps/iTV/` 目录下，运行 `mmm device/hisilicon/konka/packages/apps/iTV` 生成已经系统签名的apk，存放在 `out/target/product/Hi3796MV200/system/app/iTV/iTV.apk`

- adb连接开发板，将apk push到 `/system/app/iTV/iTV.apk`，重启开发板运行 



