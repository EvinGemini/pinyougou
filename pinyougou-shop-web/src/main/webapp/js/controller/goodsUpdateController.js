/*****
 * 定义一个控制层 controller
 * 发送HTTP请求从后台获取数据
 ****/
app.controller("goodsUpdateController",function($scope,$http,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){

    //继承父控制器
    $controller("baseController",{$scope:$scope});

    $scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态

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
    $scope.getById=function(){
        var id = $location.search()["id"];
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(function(response){
            //将后台的数据绑定到前台
            $scope.entity=response;

            //查询2级分类
            $scope.selectItemCat2List($scope.entity.category1Id);
            $scope.selectItemCat3List($scope.entity.category2Id);
            //加载富文本
            editor.html($scope.entity.goodsDesc.introduction);
            //转图片格式
            $scope.entity.goodsDesc.itemImages = angular.fromJson($scope.entity.goodsDesc.itemImages);
            //转扩展属性
            $scope.entity.goodsDesc.customAttributeItems=angular.fromJson($scope.entity.goodsDesc.customAttributeItems);
            //转规格属性
            $scope.entity.goodsDesc.specificationItems = angular.fromJson($scope.entity.goodsDesc.specificationItems);
            //转items的spec属性
            $scope.entity.items.spec=angular.fromJson($scope.entity.items.spec);
            $.each($scope.entity.items,function (index, element) {
                element.spec=angular.fromJson(element.spec);
            })

        });
    };

    $scope.checkAttributeValue = function(attributeName,attributeValue) {
        var result = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",attributeName);
        if (result != null && result.attributeValue.indexOf(attributeValue) >= 0) {
          return true;
        }
        return  false;
    };

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

    $scope.entity={goodsDesc:{itemImages:[],specificationItems:[]}};

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

    //监控模板id变化
    $scope.$watch("entity.typeTemplateId",function (newValue, oldValue) {
        if (newValue > 0) {
            typeTemplateService.findOne(newValue).success(function (response) {
                $scope.entity.typeTemplate.brandIds = angular.fromJson(response.brandIds);
                if ($location.search()['id'] == null) {
                    $scope.entity.goodsDesc.customAttributeItems=angular.fromJson(response.customAttributeItems);
                }
            });
            typeTemplateService.getSpecList(newValue).success(function (response) {
                $scope.specList=response;
            });
        }else {
            //清空品牌数据
            $scope.entity.typeTemplate= {brandIds:[]};
            if ($location.search()['id'] == null) {
                $scope.entity.goodsDesc.customAttributeItems = [];
            }
            //清空规格选项
            $scope.specList={};
        }

    })

    $scope.updateSpecAttribute=function ($event, name, value) {
        var attr = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",name);
        if (attr != null) {
            //具有该属性
            if ($event.target.checked) {
                attr.attributeValue.push(value);
            } else {
                var valueIndex = attr.attributeValue.indexOf(value);
                attr.attributeValue.splice(valueIndex,1);
                if (attr.attributeValue.length <= 0) {
                    var nameIndex = $scope.entity.goodsDesc.specificationItems.indexOf(attr);
                    $scope.entity.goodsDesc.specificationItems.splice(nameIndex,1);
                }
            }

        }else {
            //不具有该属性
            attr={attributeName:name,attributeValue:[value]};
            $scope.entity.goodsDesc.specificationItems.push(attr);
        }
    }

    //创建一条item
    $scope.createItemList=function() {
        //单个商品
        var item={spec:{},price:0,num:1,status:'0',isDefault:'0'};
        //集合商品
        $scope.entity.items=[item];
        //重新定义一个变量，写法更简洁
        var speclist = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < speclist.length; i++) {
            $scope.entity.items = addColumn($scope.entity.items,speclist[i].attributeName,speclist[i].attributeValue);
        }
    }


    //重组添加属性
    addColumn=function (list, attributeName, attributeValue) {
        //定义一个新的集合
        var newList=[];
        //循环原来商品
        for (var i = 0; i < list.length; i++) {
            //重组
            for (var j = 0; j < attributeValue.length; j++) {
                //深克隆
                var newItem = angular.copy(list[i]);
                //给原来的item添加属性
                newItem.spec[attributeName] = attributeValue[j];
                //放入另外一个新的集合中
                newList.push(newItem);
            }
        }
        return newList;
    }

    //所有分类接收集合定义
    $scope.itemCatList={};
    $scope.findItemCatList=function () {
        itemCatService.findAllList().success(function (response) {
            for (var i = 0; i < response.length; i++) {
                $scope.itemCatList[response[i].id]=response[i].name
            }
        })
    }

});
