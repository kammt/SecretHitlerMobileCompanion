console.log(location.host);

const getRequest = () => {
    const Http = new XMLHttpRequest();
    const url=`http://${location.host}` ;
    Http.open("GET", url);
    Http.send();

    Http.onreadystatechange = (e) => {
        console.log(Http.responseText)
    }
};

//setInterval(getRequest, 2000);