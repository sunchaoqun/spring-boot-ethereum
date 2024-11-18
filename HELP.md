curl -X POST http://localhost:8888/api/transaction \
-H "Content-Type: application/json" \
-d '{
    "operation": "status"
}'

curl -X POST --data '{
    "jsonrpc":"2.0",
    "method":"eth_getTransactionCount",
    "params":["0x2E59645ab79f11CD853871BEd5e21EbE2744640d", "latest"],
    "id":1
}' -H "Content-Type: application/json" https://holesky.infura.io/v3/01563559eadd46efa145cc2cd225f72e

curl -X POST http://localhost:8080/api/transaction \
-H "Content-Type: application/json" \
-d '{
    "operation": "sign", 
    "amount": 0.001, 
    "dstAddress": "0x8a9A676c2481B14044851eB0CCA166092100Be23", 
    "nonce": 5,
    "type": 2,
    "chainId": 17000,
    "maxFeePerGas": 100000000000,
    "maxPriorityFeePerGas": 3000000000 
}'

curl -X POST --data '{
    "jsonrpc":"2.0",
    "method":"eth_sendRawTransaction",
    "params":["0x02f8758242680584b2d05e0085174876e80083027100948a9a676c2481b14044851eb0cca166092100be2387038d7ea4c6800000c080a0d8ebd7c76f155577e7f43b2e09916a521d8012d2be7fcba04561cb3b98924d9ca03f905dc31bef3b9aaec827194f18fbeebda62207000401b77f9b23f046adc604"],
    "id":1
}' -H "Content-Type: application/json" https://holesky.infura.io/v3/01563559eadd46efa145cc2cd225f72e


0x0be85caca5f82913f4f91f2533e7240bc78f07027cf2c329e045838a42b424d7

aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws


c91103b6-d704-4d19-9d6a-d1114e849bc2


8c7c0889-2b8a-4935-84d9-59575a10623a