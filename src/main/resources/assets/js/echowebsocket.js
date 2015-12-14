var wshost = location.origin.replace(/^http/, 'ws');
var wspath ="/ratpacksample/echo/echows";
var wsout;

function init() {
    wsout = document.getElementById("wsout");
    wsconnect();
}

function wsconnect() {
    websocket = new WebSocket(wshost+wspath);
    websocket.onopen = function (evt) {
        onOpen(evt)
    };
    websocket.onclose = function (evt) {
        onClose(evt)
    };
    websocket.onmessage = function (evt) {
        onMessage(evt)
    };
    websocket.onerror = function (evt) {
        onError(evt)
    };
}

function onOpen(evt) {
    outputHtml("CONNECTED");
    doSend("I wonder what this sentence would look like when vowels are messed up?");
}

function onClose(evt) {
    outputHtml("DISCONNECTED");
}

function onMessage(evt) {
    outputHtml('<span style="color: blue;">RESPONSE: ' + evt.data + '</span>');
    websocket.close();
}

function onError(evt) {
    outputHtml('<span style="color: red;">ERROR:</span> ' + evt.data);
}

function doSend(message) {
    outputHtml("SENT: " + message);
    websocket.send(message);
}

function outputHtml(message) {
    var pre = document.createElement("p");
    pre.style.wordWrap = "break-word";
    pre.innerHTML = message;
    wsout.appendChild(pre);
}

window.addEventListener("load", init, false);

