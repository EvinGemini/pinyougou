app.controller("loginController",function ($scope,loginService) {
    $scope.getUserName = function () {
        loginService.getUserName().success(function (data) {
            $scope.userLoginName=data;
        });
    }
});