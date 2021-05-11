//mock API
if(!app)
	var app={
		siguienteGeneracion:function(){
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
	};

function getRandomRows(a){
	let res=[];
	for(let i=0;i<a;i++)
		res.push(new Array(3).fill(Math.random()));
	return res;
}

//carga Google Charts
var grafico={chart:null,data:null};
google.charts.load('current', {'packages':['line']});
google.charts.setOnLoadCallback(()=>{
	var $table = $('#table');
	let data=crearData();

    /*data.addRows([
      [1,  1, 0.5, 0.1],
      [2,  1, 0.5, 0.1],
      [3,  1, 0.5, 0.1],
      [4,  1, 0.5, 0.1],
      [5,  1, 0.5, 0.1],
      [6,  1, 0.5, 0.1],
      [7,  1, 0.5, 0.1],
      [8,  1, 0.5, 0.1],
      [9,  1, 0.5, 0.1],
      [10, 1, 0.5, 0.1],
      [11, 1, 0.5, 0.1],
      [12, 1, 0.5, 0.1],
      [13, 1, 0.5, 0.1],
      [14, 1, 0.5, 0.1],
      [15, 1, 0.5, 0.1],
      [16, 1, 0.5, 0.1],
      [17, 1, 0.5, 0.1],
      [18, 1, 0.5, 0.1],
      [19, 1, 0.5, 0.1],
      [20, 1, 0.5, 0.1],
      [21, 1, 0.5, 0.1],
      [22, 1, 0.5, 0.1],
      [23, 1, 0.5, 0.1],
      [24, 1, 0.5, 0.1],
      [25, 1, 0.5, 0.1],
      [26, 1, 0.5, 0.1],
      [27, 1, 0.5, 0.1]
    ]);*/
// Specify the JSON data to be displayed
var tableData =
[
{
  "gen": 1,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 2,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 3,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 4,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 5,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 6,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 7,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 8,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 9,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 10,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 11,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 12,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 13,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 14,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 15,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 16,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 17,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 18,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 19,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
},
{
  "gen": 20,
        "max": "1",
        "prom": "0.5",
        "min": "0.1"
}
];
    
    var options = {
      chart: {
        title: 'Generaciones',
        subtitle: '20',
        colors: ['#e0440e', '#e6693e', '#ec8f6e'],
      
      },
      hAxis: {
        title: 'Fitness'
      },
      explorer: {
        actions: ['dragToZoom', 'rightClickToReset'],
        axis: 'horizontal'
      },
      vAxis: {
        title: 'Generaciones'
      },
      height: 450,
      width: 600 
    };
  let chart = new google.charts.Line(document.getElementById('generacionesChart'));
  chart.draw(data, google.charts.Line.convertOptions(options));
	grafico={chart,data};
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
	grafico.chart.load(grafico.data);
}

//important methods
var tableData=[];
var gEt=document.getElementById;
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
function reiniciar(primeraGeneracion){
	grafico.data=crearData();
	//borrar tabla
	proximaGeneracion(primeraGeneracion);
}
function proximaGeneracion(generacion){
	generaciones.push(generacion);
	grafico.data.addRow([
		generaciones.length
		//,
	]);
	//añadir al grafico
	//añadir a la tabla
}
/*No vale la pena
function previaGeneracion(){
	//mostrarGeneracion(generacionActual)
	//quitar del grafico
	//añadir a la tabla
}
function mostrarGeneracion(indice){

}*/

//onload
addEventListener('DOMContentLoaded',()=>{
	$('table').bootstrapTable({
		data: tableData
	});

	$('#modalAjuste').modal('show');

	gEt('modal-iniciar').onclic=()=>{
		//reestablecer el gráfico
		//reestablecer la tabla
		//reestablecer registros
		app.iniciarSimulacion(
			gEt('modal-individuos').value
			,gEt('modal-seleccion').value
			,gEt('modal-elitismo').value
		);
	};
});
