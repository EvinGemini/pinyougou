app.service("loginService",function($http){

    //查询用户名
    this.getUserName=function(){
        return $http.post("/login/name.shtml");
    }

});
