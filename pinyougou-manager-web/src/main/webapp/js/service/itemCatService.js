/***
 * 创建一个服务层
 * 抽取发送请求的一部分代码
 * */
app.service("itemCatService",function($http){

    //查询列表
    this.findAll=function(page,size,searchEntity){
        return $http.post("/itemCat/list.shtml?page="+page+"&size="+size,searchEntity);
    }

    //增加ItemCat
    this.add=function(entity){
        return $http.post("/itemCat/add.shtml",entity);
    }

    //保存
    this.update=function(entity){
        return $http.post("/itemCat/update.shtml",entity);
    }

    //根据ID查询
    this.findOne=function(id){
        return $http.get("/itemCat/"+id+".shtml");
    }

    //批量删除
    this.delete=function(ids){
        return $http.post("/itemCat/delete.shtml",ids);
    }

    //根据父id查询ItemCat
    this.getByParentId=function (id) {
        return $http.post("/itemCat/parent/" + id + ".shtml");
    }

});
