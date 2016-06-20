# JXPi
JXPi平台是可以用于微小智能系统的智能控制系统，可实现诸如通过web页面对树莓派进行控制这样的功能。

其设计目标是实现一个稳定可靠、可动态配置、弹性扩充的智能控制系统。

为实现这一目标，JXPi平台采用了lua+java+python三者相融合的机制

1、lua脚本用于实际控制功能的编写，系统用其实现动态配置、弹性扩充。笔者在开发一个控制系统时，需要在开发机上写好程序、编译、然后上传到连接了各种器件的测试机、停止之前的系统，运行新程序进行测试。有时一个小时内就需重复多次，效率太过低下。在后期需增加功能时也要将正在运行中的生产机停下来，造成系统失效，在应用系统中这可能还可以接受，但对于现场控制系统来说可能就无法接受了。所以就产生了将稳定可靠的平台和各种控制功能相分离的想法，最终实现为现在的JXPi平台。进一步的，JXPi平台还引入了配置项版本的概念，系统会定时检查各项配置，如果其版本号增加了，则系统会自动将之前的配置清除并导入新的配置，而这一过程一是自动的，二是隔离的，即如果新的配置出了问题也不会影响到其它部分的正常工作，由此，JXPi平台就具有了运行中切换与升级的能力。

2、java用于提供一个稳定而可靠的系统平台，并提供了丰富的基础设施。JXPi平台的主体是用java开发，提供了基本的web服务、数据库（sqlite，也支持其它数据库）访问、网络通信、外部设备访问接口、各部件勾连通道等。系统平台的首要目标是为控制系统提供一个稳定而可靠的基础平台，由于已将繁杂的控制功能从系统平台中移出，所以该平台是非常坚固的。同时，系统还提供了一些非常有用的功能，如状态机、异步操作、json、ORM等等。系统平台主要提供了如下的功能：

- 外部访问接口：系统以REST方式提供了可供web或其它外部设备访问的系统接口，系统已构建了完整的REST接口框架，可以快速简便的按需提供REST接口

- 各组成部分的勾连：系统打通了系统和lua、python、前端之间的沟通通道，使得各部分能够有机的融合为一体，用于控制的lua脚本在系统平台以及后端python的支持下终将无所不能

- 强大的扩展能力：JXPi平台提供了简便的机制可通过lua/python进行定制性的扩展，详见附录四中的说明

- 前端设备的勾连：在一个互联的世界里，除了需要被其他人或系统访问，也需要访问其它的设备或系统，系统将对外部设备的访问封装为lua模块，使用者可以直接在lua脚本中实现对前端设备的访问和控制

- 通常的系统管理：日志、访问控制、数据库等等

3、python用于在后台充分利用python所积累的丰富资源来扩充系统的功能。利用JXPi平台提供的非常简便的扩展机制，可方便的将python代码集成到JXPi平台中来。

简单的讲，JXPi平台是希望提供一个稳定、可靠、坚固的控制系统平台，然后在这个基础之上提供了各种简便的扩展接口，可以简便的将各种资源加以快速而低成本的黏合。

JXPi平台同时提供了lua接口和REST接口，摆脱前端界面，JXPi平台可以作为哑终端完全通过lua脚本检测输入信号来实现普通的控制系统的工作。而利用REST接口，则可通过web、手机、其它智能系统等前端实现对其进行可视化操作。作为一个整体，JXPi平台在发布时，提供了一个web的访问前端，在系统运行后，可直接通过web进行访问。

JXPi平台的使用：直接运行命令即可:

sudo java -jar jxpi.jar &

## 版本说明

**v0.3.0**

- 修复了LRU的bug，该LRU用于缓存通过GetByID读取的ORM对象，从而降低数据库的select操作。

**v0.2.0**

- 修复了数据库多表读取时的重大bug，此bug为系统bug，多表联合查询时如有重名的列会导致数据错误。

- 增加了数据库增量添加ORM数据表的功能。本功能将使得根据需要在用户开发自己的升级版本时，当需要增加数据表时，不必手动建表。

	**注1**：本功能目前只能对新增的ORM数据表进行自动创建，对已建表的数据列的修改则无法实现自动修改

    **注2**：本功能在本版本的升级时会导致创建的所有数据表全部被删除重建！！严重警告：如果已在旧版本下增加了数据，需在升级前将main.db进行备份，等升级后，将原main.db复制回来。由于本手册并未使用到数据的存取，因此，可忽略该警告。根据本版本升级后的系统将不会再出现该问题


