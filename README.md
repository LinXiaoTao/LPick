# LPick
#### 这是个多选图片选择器
#### 效果图
![LPick](https://github.com/LinXiaoTao/LPick/blob/master/gif/lpick.gif)
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
//开启选择图库
LPick.getInstance()
                .withPickCount(5) //最多选择图片
                .withSpanCount(4) //配置列数
                .pick(this,REQUEST_CODE);
//开启裁剪
 LPick.getInstance()
                 .useSourceImageAspectRatio()
                 .crop(PickImgSimpleActivity.this,Uri.fromFile(new File(model.mImgPath)),createUriSave());

```
```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data)
{
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
    {
        //获取选择的图片
        mPickModelList = data.getParcelableArrayListExtra(Constances.PICK_SOUCRE_KEY);
        mAdapter.notifyDataSetChanged();
    }else if (resultCode == RESULT_OK && requestCode == LPick.REQUEST_CROP)
    {
        //获取裁剪结果
        Uri output = LPick.getOutput(data);
        Logger.d("裁剪结果:" + output.getPath());
    }
}
```
#### 重要
关于图片的压缩，因为没有相关的知识，所有都是按不超过最大尺寸，等比例缩放来压缩图片。如果有好建议，请提issues,谢谢
```
//裁剪载入最大尺寸
public final static int MAX_CROP_SIZE = 1500;
//裁剪输出最大尺寸
public final static int MAX_RESULT_SIZE = 1000;
//小图最大尺寸
public final static int MAX_THUMB_SIZE = 300;
//大图最大尺寸
public final static int MAX_BIG_SIZE = 800;
```
这个很完善的项目，如果用在项目上，可以试试这个[TelegramGallery](https://github.com/TangXiaoLv/TelegramGallery)


##### 项目还在更新中...
