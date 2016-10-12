function callHello() {
    $.ajax({
               url: 'https://localhost:9292/pets-store/apis/hello',
               type: "GET",
               async: false,
               dataType: "text"
           }).success(function (data) {
        console.log('Success: ', data);
    }).error(function () {
        console.log('Error');
    });
}
