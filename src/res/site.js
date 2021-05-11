google.charts.load('current', {'packages':['line']});
google.charts.setOnLoadCallback(()=>{
	var data = new google.visualization.DataTable();
	data.addColumn('number', 'Generacion');
	data.addColumn('number', 'Máximo');
	data.addColumn('number', 'Promedio');
	data.addColumn('number', 'Mínimo');
});

function getRandomRows(a){
	let res=[];
	for(let i=0;i<a;i++)
		res.push(new Array(3).fill(Math.random()));
	return res;
}

var tableData=[];

function siguienteGeneracion(){
	let nuevaGeneracion=getRandomRows(1);
	tableData.push(nuevaGeneracion);

	$('table').bootstrapTable({
		data:tableData.map((el,i)=>({
			gen:i+1
			,max:el[0]
			,prom:el[1]
			,min:el[2]
		}))
	});
}

addEventListener('DOMContentLoaded',()=>{
	document.getElementById('ruleta').onclick=()=>{
		try{
			
			document.getElementById('inicio-result').innerHTML+=
				//typeof app.hello();
				eval(document.getElementById('texto').value);
				//document.getElementById('app').style.display='block';
				//document.getElementById('inicio').style.display='none';
	}catch(e){
	
		document.getElementById('inicio-result').innerHTML+=e
	}
	};

  //Espero no me baneen por esto xD

  document.getElementById('rango').onclick=()=>{
		document.getElementById('inicio-result').innerHTML='';
	};
	
//Object.getOwnPropertyNames(obj).filter(item => typeof obj[item] === 'function')

});

// setInterval(()=>appa.log("omama"),1000);
// console.debug=console.log=console.info=console.error=(e)=>document.getElementById('inicio-result').innerHTML+=e;