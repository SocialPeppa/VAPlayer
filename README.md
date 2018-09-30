# VAPlayer
一个简单手机本地视频音频和网络视频音频播放器，集成了第三方视频解码库，网络视频用的是时光网接口，网络音乐是某音乐的排行榜接口。

一.视频播放器

1.自动生成本地视频预览画面
2.视频自适应横竖屏，不断播
3.不支持的视频格式 ，自动切换到集成的第三方播放器 
4.主动更换系统播放器或集成第三方播放器
5.网络视频支持顶部下拉刷新，底部上拉加载更多
6.左边滑动调节亮度，右边滑动调节音量
7.自动隐藏控制面板，单击显示
8.双击切换横竖屏，长按暂停或开始
9.自定义状态栏显示电量时间
10.网络是视频的等待时提示网络速度
.....

二.音乐播放器

1.自定义aidl 创建服务 
2.通知栏提醒当前播放歌曲
3.点击通知栏进入当前播放歌曲
4.点击当前正在播放歌曲，不重新播放
5.自定义seekBar
6.三种循环模式：单曲循环、全部循环、顺序播放。本地保存当前模式