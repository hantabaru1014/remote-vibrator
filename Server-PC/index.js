const { createServer } = require('http');
const { WebSocketServer, OPEN } = require('ws');
const { networkInterfaces } = require('os');

let wss = null;

function vibrate(deviceName, millisec, amplitude){
  if (!wss) return;
  for (const client of wss.clients){
    if (client.readyState === OPEN && client?.deviceName === deviceName){
      client.send(JSON.stringify({cmd: 'vibrate', ms: millisec, amp: amplitude}));
    }
  }
}

const server = createServer((req, res) => {
  const url = new URL(req.url, `http://${req.headers.host}`);
  if (url.pathname === '/vibrate'){
    const params = url.searchParams;
    vibrate(params.get('name'), params.get('ms'), params.get('amp')); // amp: 1 - 255
    console.log(`Vibrate: ${params}`);
    res.writeHead(200, {'Access-Control-Allow-Origin': '*'});
    res.end('OK');
  }
});
wss = new WebSocketServer({ server });

wss.on('connection', ws => {
  ws.on('message', data => {
    const recvData = JSON.parse(data);
    if (recvData?.cmd === 'setDeviceName'){
      ws.deviceName = recvData.name ?? 'Unknown';
      console.log(`SetDeviceName: ${ws.deviceName}`);
    }
  });
  ws.on('close', () => {
    console.log(`Disconnected Device:${ws.deviceName}`);
  });
  console.log(`Connected New Device`);
});

const port = process.argv[2] ?? 6614;
server.listen(port);
const lanIP = Object.values(networkInterfaces()).flat().find(i => i.family == 'IPv4' && !i.internal).address;
console.log(`IP: ${lanIP}`);
console.log(`Port: ${port}`);