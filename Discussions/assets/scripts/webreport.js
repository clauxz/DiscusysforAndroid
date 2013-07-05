

var ids=new Array('basicInfo','participants','bg','media','sources','finalBoard','summary','argPoints','clustInfo','linkInfo');

var cont = document.getElementsByClassName('toc').item(0);
var links = cont.getElementsByTagName('a');

for (var i = 0; i < links.length; i++){
	
	window.JSIR.onPrintInfo(ids[i],links[i].innerText);
	
	
	links[i].addEventListener("click",function(evt){
		evt.preventDefault();
		
		window.JSIR.fireEvent("onPrintInfo",links[i].href);
	});    
};	