# LPick
#### 这是个多选图片选择器

##### 感谢
这个项目是基于[RxGalleryFinal](https://github.com/FinalTeam/RxGalleryFinal)的二次开发,使用到的开源项目如下:
* [PermissionsDispatcher](https://github.com/hotchemi/PermissionsDispatcher) 处理动态权限申请
* [uCrop](https://github.com/Yalantis/uCrop) 非常好用的图片裁剪库
* [picasso](https://github.com/square/picasso) 用来图片加载
* [PhotoView](https://github.com/chrisbanes/PhotoView) 大图显示
* [rxjava](https://github.com/ReactiveX/RxJava)和[rxandroid](https://github.com/ReactiveX/RxAndroid) 用于异步请求
* [SuperRecyclerView](https://github.com/Malinskiy/SuperRecyclerView) 列表加载更多
* [leakcanary](https://github.com/square/leakcanary) 内存泄漏检查
* [butterknife](https://github.com/JakeWharton/butterknife) View绑定
* [logger](https://github.com/orhanobut/logger) log日志打印

##### 用法如下:
```
LPick.getInstance()
                .withPickCount(5) //最多选择图片
                .withSpanCount(4) //配置列数
                .pick(this,REQUEST_CODE);
```
```
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            //获取到数据格式为 List<PickModel>
            data.getParcelableArrayListExtra(Constances.PICK_SOUCRE_KEY);
        }
    }
```

##### 项目还在更新中...