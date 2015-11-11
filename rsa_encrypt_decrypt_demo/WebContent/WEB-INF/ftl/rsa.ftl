<#import "spring.ftl" as spring />
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>    	
	<title>RSA Demo</title>
</head>
<body>

<p>请输入要加密的内容： <input type="text" id="raw_content"/ value="测试内容a"/></p>
<p><button id="btn" onclick="testEncryptDecrypt()">加密</button></p>
<p></p>
<p>服务端返回的内容</p>
<p id="p_result"></p>

<script src="http://apps.bdimg.com/libs/jquery/1.9.1/jquery.min.js"></script>
<script type="text/javascript" src="../js/Barrett.js"></script>
<script type="text/javascript" src="../js/BigInt.js"></script>
<script type="text/javascript" src="../js/RSA.js"></script>

<script>
	function testEncryptDecrypt(){
		var raw_content = $("#raw_content").val();
		setMaxDigits(130); //1024位就是130，2048位就是260.。。。。。。
		var key1 = new RSAKeyPair("${e}","","${n}");	//从服务端获取到的n和e可以得出公钥
		var encode_content = encodeURIComponent(raw_content);
		console.log("encodeURIComponent后的值");
		console.log(encode_content);
		var encryptData = encryptedString(key1,encode_content);
		console.log("客户端公钥加密后的值");
		console.log(encryptData);
		var reqUrl = "rsa?action=test&data=" + encryptData;
		$.ajax({
		type:"get",
		dataType:"json",
		url:reqUrl,
		complete:function(data){
			var jsonData = eval("("+data.responseText+")");
			var t1 = jsonData.encryptResult;
			console.log("服务端加密后的值");
			console.log(t1);
			var key2 = new RSAKeyPair("", "${e}", "${n}");
			var data2 = decryptedString(key2, t1);
			console.log("服务端加密, 客户端解密后的值");
			console.log(data2);
			var data3 =  reverse(data2);		//解密后的结果是倒序排列- -
			console.log("服务端加密, 客户端解密后的值, 再倒序一下");
			console.log(data3);
			var data4 = decodeURIComponent(data3);
			console.log("最后decodeURIComponent一下");
			console.log(data4);
			$("#p_result").html(data4);
		}
		});
	}
	
	//倒序返回
	function reverse(s){
    	return s.split("").reverse().join("");
	}
	
	
</script>
</body>
</html>