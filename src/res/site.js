//mock API
if(!app)
	var app={
		siguienteGeneracion:function(){
			proximaGeneracion({
				individuos:new Array(10).fill().map((el,i)=>[i+1,  1, 0.5, 0.1])
				,min:0
				,pro:.5
				,max:Math.random()
			});
		}
		,iniciarSimulacion:function(){
			proximaGeneracion({
				individuos:new Array(10).fill().map((el,i)=>[i+1,  1, 0.5, 0.1])
				,min:0
				,pro:.5
				,max:1
			});
		}
	};

//Carga Google Charts
var grafico={chart:null,data:null};
google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(()=>{
	grafico={
		chart:new google.visualization.LineChart(document.getElementById('generacionesChart'))
		,data:crearData()
	};
});
function crearData(){
	let data = new google.visualization.DataTable();
	data.addColumn('number', 'Generacion');
	data.addColumn('number', 'Máximo');
	data.addColumn('number', 'Promedio');
	data.addColumn('number', 'Mínimo');
	return data;
}
function actualizarGrafico(){
	grafico.chart.draw(
		grafico.data
		,{
			title: 'Fitness mínima, máxima y promedio de las generaciones.',
			subtitle: '20',
	
			hAxis: {
				title: 'Generación'
			},
			explorer: {
				actions: ['dragToZoom', 'rightClickToReset'],
				axis: 'horizontal'
			},
			vAxis: {
				title: 'Fitness'
				,minValue:0
				,maxValue:1.1
			}
			,
			height: 450,
			width: 600
			,pointSize:5
			,legend:'none'
		}
	);
}

//important methods
var tableData=[];
var gEt=id=>document.getElementById(id);
var qS=selector=>document.querySelectorAll(selector);
var generaciones=[];
/*formato de la generación:
{
	individuos:[
		{
			fitness:
			,cromosoma:
			,valorDecimal:
		}
	]
	,min:
	,pro:
	,max:
}
*/
function proximaGeneracion(generacion){
	generaciones.push(generacion);
	grafico.data.addRow([
		generaciones.length
		,generacion.max
		,generacion.pro
		,generacion.min
	]);
	actualizarGrafico();
	//añadir a la tabla
}

//onload
addEventListener('DOMContentLoaded',()=>{
	$('table').bootstrapTable({
		data: tableData
	});

	$('#modalAjuste').modal('show');

	gEt('modal-iniciar').onclick=()=>{
		grafico.data=crearData();
			//borrar tabla
		app.iniciarSimulacion(
			gEt('modal-individuos').value
			,qS('[name="ruleta"]:checked').value
			,gEt('modal-elitismo').value
		);
	};

	gEt('controles-siguiente').onclick=()=>{
		for(let i=0,to=gEt('controles-pasos').value;i<to;i++)
			app.siguienteGeneracion();
	}
});