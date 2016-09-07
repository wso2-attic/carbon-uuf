function callHello() {
    console.log('In hello page');
    // var userName1=userName;
    $.ajax({
               url: 'https://localhost:9292/pets-store/hello/say',
               type: "GET",
               async: false,
               dataType: "text"
           }).success(function (data) {
        console.log('Success: ', data);
    }).error(function () {
        console.log('Error');
    });
}