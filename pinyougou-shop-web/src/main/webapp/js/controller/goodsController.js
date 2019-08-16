/*****
 * 定义一个控制层 controller
 * 发送HTTP请求从后台获取数据
 ****/
app.controller("goodsController",function($scope,$http,$controller,goodsService,uploadService,itemCatService){

    //继承父控制器
    $controller("baseController",{$scope:$scope});

    //获取所有的Goods信息
    $scope.getPage=function(page,size){
        //发送请求获取数据
        goodsService.findAll(page,size,$scope.searchEntity).success(function(response){
            //集合数据
            $scope.list = response.list;
            //分页数据
            $scope.paginationConf.totalItems=response.total;
        });
    }

    //添加或者修改方法
    $scope.save = function(){
        var result = null;
        if($scope.entity.id!=null){
            //执行修改数据
            result = goodsService.update($scope.entity);
        }else{
            $scope.entity.goodsDesc.introduction=editor.html();
            //增加操作
            result = goodsService.add($scope.entity);
        }
        //判断操作流程
        result.success(function(response){
            //判断执行状态
            if(response.success){
                //重新加载新的数据
                alert("增加成功");
                $scope.entity={};
                editor.html("");
            }else{
                //打印错误消息
                alert(response.message);
            }
        });
    }

    //根据ID查询信息
    $scope.getById=function(id){
        goodsService.findOne(id).success(function(response){
            //将后台的数据绑定到前台
            $scope.entity=response;
        });
    }

    //批量删除
    $scope.delete=function(){
        goodsService.delete($scope.selectids).success(function(response){
            //判断删除状态
            if(response.success){
                $scope.reloadList();
            }else{
                alert(response.message);
            }
        });
    };

    $scope.entity={goodsDesc:{itemImages:[]}};

    //图片上传
    $scope.uploadFile=function () {
        uploadService.uploadFile().success(function (response) {
            uploadService.uploadFile().success(function (response) {
                if(response.success){
                    //设置图片访问地址
                    $scope.image_entity.url=response.message;
                }else{
                    alert(response.message);
                }
            }).error(function() {
                alert("上传发生错误");
            });
        });
    };

    //增加图片到列表中
    $scope.add_image_entity=function(){
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    };

    $scope.remove_image_entity=function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    };

    //查询商品一级分类
    $scope.selectItemCat1List = function (id) {
        itemCatService.getByParentId(id).success(function (response) {
            $scope.itemCat1List=response;
            $scope.itemCat2List= null;
            $scope.itemCat3List= null;
            $scope.entity.typeTemplateId=null;
        })
    };
    //查询商品二级分类
    $scope.selectItemCat2List = function (id) {
        itemCatService.getByParentId(id).success(function (response) {
            $scope.itemCat2List=response;
            $scope.itemCat3List= null;
            $scope.entity.typeTemplateId=null;
        })
    };

    //查询商品三级分类
    $scope.selectItemCat3List = function (id) {
        itemCatService.getByParentId(id).success(function (response) {
            $scope.itemCat3List=response;
        })
    };

    //监控三级分类id的变化，查询模板id
    $scope.$watch("entity.category3Id",function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.typeTemplateId=response.typeId;
        })
    })
});
