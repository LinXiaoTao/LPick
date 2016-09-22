# LPick
#### 这是个多选图片选择器

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