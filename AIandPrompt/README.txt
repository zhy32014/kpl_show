使用时应到deepseek官网注册并获取api密钥，且需要充值，可充值1元。
将api密钥复制到python项目中的 .env 文件的DEEPSEEK API_KEY中，运行main.py。
运行成功后，在浏览器中打开http://127.0.0.1:8000/docs，若访问成功，则AI接口已打开，后端程序即可通过相应的 http 操作来获取AI服务。

对于Prompt，有多种版本可使用。想要换不同的版本，可到python项目中的kpl-prompts.py文件的相应位置替换。