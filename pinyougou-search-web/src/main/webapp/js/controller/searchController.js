/*
* 搜索Controller实现
* */
app.controller('searchController',function ($scope,searchService) {
    //搜索兑现
    $scope.searchMap={"keyword":"","category":"","brand":"","spec":{}};

    //搜索方法
    $scope.search=function () {
        searchService.search($scope.searchMap).success(function (response) {
            //$scope.list=response.rows;
            $scope.resultMap=response;
        })
    }

    //添加搜索条件方法
    $scope.addSearchItem=function (key, value) {
        if (key == "category" || key == "brand") {
            $scope.searchMap[key] = value;
        }else {
            $scope.searchMap.spec[key]=value;
        }
        //这里做提交搜索
        $scope.search();
    }

    //删除搜索条件方法
    $scope.removeSearchItem=function (key) {
        if (key == "category" || key == "brand") {
            $scope.searchMap[key] = "";
        }else {
            delete $scope.searchMap.spec[key];
        }
        //这里做提交搜索
        $scope.search();
    }
});