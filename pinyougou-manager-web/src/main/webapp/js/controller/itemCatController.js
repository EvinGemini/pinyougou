/*****
 * 定义一个控制层 controller
 * 发送HTTP请求从后台获取数据
 ****/
app.controller("itemCatController",function($scope,$http,$controller,itemCatService){

    $scope.entity_1={"name":"顶级分类列表","parentId":0};
    $scope.entity_2=null;
    $scope.entity_3=null;


    $scope.parentId=0;



    $scope.grade = 1;

    $scope.loadChild=function (item) {
        $scope.grade+=1;
        if ($scope.grade == 2) {
            $scope.entity_2 = item
            $scope.entity_3=null;
        } else if ($scope.grade == 3) {
            $scope.entity_3 = item
        }


    }

    //继承父控制器
    $controller("baseController",{$scope:$scope});

    //获取所有的ItemCat信息
    $scope.getPage=function(page,size){
        //发送请求获取数据
        itemCatService.findAll(page,size,$scope.searchEntity).success(function(response){
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
            result = itemCatService.update($scope.entity);
        }else{
            $scope.entity.parentId=$scope.parentId;//赋予上级ID
            //增加操作
            result = itemCatService.add($scope.entity);
        }
        //判断操作流程
        result.success(function(response){
            //判断执行状态
            if(response.success){
                //重新加载新的数据
                $scope.getByParentId($scope.parentId);//重新加载
            }else{
                //打印错误消息
                alert(response.message);
            }
        });
    }

    //根据ID查询信息
    $scope.getById=function(id){
        itemCatService.findOne(id).success(function(response){
            //将后台的数据绑定到前台
            $scope.entity=response;
        });
    }

    //批量删除
    $scope.delete=function(){
        itemCatService.delete($scope.selectids).success(function(response){
            //判断删除状态
            if(response.success){
                $scope.reloadList();
            }else{
                alert(response.message);
            }
        });
    }

    //根据父id查询ItemCat
    $scope.getByParentId=function (parentId) {
        $scope.parentId = parentId;
        itemCatService.getByParentId(parentId).success(function (response) {
            $scope.list = response;
        })

    }
});
