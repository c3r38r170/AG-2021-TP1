import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import javafx.concurrent.Worker.State;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Consumer;

public class App extends JFrame {
	
	private static final long serialVersionUID = 1L;

	private boolean elitismo=false;
	private boolean seleccionPorRango=false;
	
	private LinkedList<Individuo[]> generations=new LinkedList<>();
	private Individuo[] poblacionActual;
	
	private Individuo maximoIndividuo;
	// private double maximoObjetivo=0;
	private Individuo minimoIndividuo;
	// private double minimoObjetivo=1;
	private double promedio;
	private double sumatoriaPuntuaciones=0;
	private int viendoGeneracion=1;
	
	private int tamañoPoblacion=0;
	
	// JScrollPane tableWrapper=new JScrollPane();
	// String[] columnNames = {"Cromosoma", "Valor Decimal","Fitness"};
	// JTable table=new JTable(new Object[0][3],columnNames);
	
	// JLabel generaciones=new JLabel();
	
	private WebEngine webEngine;

	private double objetivo(Individuo individuo){
		double valorDecimal=individuo.valorDecimal;
		return Math.pow(valorDecimal/1073741823/*2^30-1*/,2);
	}
	
	public static void main(String[] args) throws Exception {
		// launch(args);
		new App();
	}
	
	// @Override
	// public void start(Stage primaryStage) throws Exception {
	public App(){
		// TODO Poner botón de iniciar
		
		// TODO ver qué hace
		// SwingUtilities.invokeLater(() -> {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
			// JButton botonAnterior=new JButton("< Anterior <");
			// botonAnterior.addMouseListener(new MouseInputAdapter(){
			// 	@Override
			// 	public void mouseClicked(MouseEvent e){
			// 		if(!botonAnterior.isEnabled())
			// 			return;
			// 		mostrarGeneracionAnterior();
			// 		if(viendoGeneracion==1)
			// 			botonAnterior.setEnabled(false);
			// 	}
			// });
			// botonAnterior.setEnabled(false);
			
			// JButton botonReiniciar=new JButton("Reiniciar");
			// botonReiniciar.addMouseListener(new MouseInputAdapter(){
			// 	@Override
			// 	public void mouseClicked(MouseEvent e){
			// 		reiniciar(10);
			// 		botonAnterior.setEnabled(false);
			// 	}
			// });
			
			// JButton botonAvanzar=new JButton("Avanzar");
			// botonAvanzar.addMouseListener(new MouseInputAdapter(){
			// 	@Override
			// 	public void mouseClicked(MouseEvent e){
			// 		if(!botonAnterior.isEnabled())
			// 			botonAnterior.setEnabled(true);
					
			// 		long cantidad=(Long)campoCantidadAvanzar.getValue();
			// 		for(int i=0;i<cantidad;i++)
			// 			nuevaGeneracion();
			// 	}
			// });
			
			// JButton botonSiguiente=new JButton("> Siguiente >");
			// botonSiguiente.addMouseListener(new MouseInputAdapter(){
			// 	@Override
			// 	public void mouseClicked(MouseEvent e){
			// 		nuevaGeneracion();
			// 		if(!botonAnterior.isEnabled())
			// 			botonAnterior.setEnabled(true);
			// 	}
			// });
		
			// 	String[] columNames = {"Cromosoma", "Valor Decimal","Fitness"};
			
		JFXPanel jfxPanel = new JFXPanel();
		add(jfxPanel);
		

		// TODO ver qué hace
		Platform.runLater(() -> {
			WebView webView = new WebView();
			jfxPanel.setScene(new Scene(webView));
			webEngine = webView.getEngine();
			webEngine.load(getClass().getResource("res/index.html").toString());
			
			App self=this;
			webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
        @Override
        public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
					if (newState == State.SUCCEEDED) {
						JSObject window = (JSObject) webEngine.executeScript("window");
						window.setMember("app", self);
					}
				}
      });
		});

		// TODO setTitle y setIcon
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width/2,screenSize.height/2);
		setLocation(screenSize.width/4, screenSize.height/4);
		setVisible(true);
		
		// TODO iniciar variables a partir de lo pasado por el frontend, Tal vez hacer un metodo iniciar()
		// solo usar pares
		this.tamañoPoblacion=10;
		// set elitismo y seleccion
		
		//reiniciar(tamañoPoblacion);
			
	}
	
	private void reiniciar(){
		
		// Reinicio de todas las variables.
		sumatoriaPuntuaciones=0;
		viendoGeneracion=1;
		generations=new LinkedList<>();
		
		// Generación de la primera población aleatoria.
		poblacionActual=new Individuo[tamañoPoblacion];
		for(int i=0;i<tamañoPoblacion;i++){
			boolean[] cromosoma=new boolean[30];
			
			for(int j=0;j<30;j++)
				cromosoma[j]=Math.random()>.5;
			
			Individuo newIndividuo=new Individuo(cromosoma);
			
			poblacionActual[i]=newIndividuo;
			
			double fitness=objetivo(poblacionActual[i]);
			poblacionActual[i].fitness=fitness;
			sumatoriaPuntuaciones+=fitness;
		}
		promedio=sumatoriaPuntuaciones/tamañoPoblacion;
		
		Arrays.sort(poblacionActual);
		generations.add(poblacionActual);
		
		// dataset.removeAllSeries();
		// serieMinimo=new XYSeries("Mínimo");
		// seriePromedio=new XYSeries("Promedio");
		// serieMaximo=new XYSeries("Máximo");
		// dataset.addSeries(serieMinimo);
		// dataset.addSeries(seriePromedio);
		// dataset.addSeries(serieMaximo);
		
		calcularMinMaxPro();
		mostrarGeneracion();
	}
	
	public void nuevaGeneracion(){
		viendoGeneracion++;
		
		if(viendoGeneracion<generations.size()+1){
			mostrarGeneracionExistente();
		}
		
		Individuo[] nuevaPoblacion=new Individuo[tamañoPoblacion];
		int cantidadPares=tamañoPoblacion/2;
		if(elitismo){
			// Reemplazamos el último par por los mejores individuos. (Recuerde que las poblaciones están ordenadas.)
			cantidadPares--;
			nuevaPoblacion[tamañoPoblacion-1]=poblacionActual[0];
			nuevaPoblacion[tamañoPoblacion-2]=poblacionActual[1];
		}
		
		double[] vectorProbabilidades=new double[tamañoPoblacion];
		Consumer<Integer> calculoDeProbabilidades=seleccionPorRango?
			// En la selección por rango, la probabilidad de crossover se basa en el rango (1ro, 2do, 3ro...) del individuo en la población.
			j->vectorProbabilidades[j]=(1.2-.4* j /(tamañoPoblacion-1))/tamañoPoblacion
			// En la selección por ruleta, usamos el fitness para calcular las probabilidades de crossover.
			:j->vectorProbabilidades[j]=poblacionActual[j].fitness/sumatoriaPuntuaciones
		;
		for(int j=0;j<tamañoPoblacion;j++)
			calculoDeProbabilidades.accept(j);
		
		sumatoriaPuntuaciones=0;
			
		// Aplicación de seleccion.
		for(int j=0;j<cantidadPares;j++){
			int j1=j*2,j2=j1+1;
			boolean elegido1=false
				,elegido2=false;
			double selector1=Math.random()
				,selector2=Math.random()
				,acc=0;
			Individuo individuo1=null
				,individuo2=null;
			//Los nulls son para que no me tire la advertencia "The local variable may not have been initialized" más abajo en la IDE.
			
			// No hay forma de que la probabilidad (selector 1 y 2) sea mayor a 1, y la suma (acc) va a llegar a 1 en algun momento.
			// Por lo que este for va a en algún momento terminar con elegido1 y elegido2.
			for(int l=0;l<tamañoPoblacion;l++){
				acc+=vectorProbabilidades[l];
				if(!elegido1 && acc>selector1){
					individuo1=poblacionActual[l];
					elegido1=true;
				}
				if(!elegido2 && acc>selector2){
					individuo2=poblacionActual[l];
					elegido2=true;
				}
				if(elegido1 && elegido2)
					break;
			}
			// Aún así, a veces por división de punto flotante, la suma no es exactamente igual a 1 y el número aleatorio puede entrar en ese margen de error.
			// Por lo que en ese caso, elegimos el último.
			// Técnicamente le estamos asignando el resto de la probabilidad a un cromosoma aleatorio, pero es una probabilidad insignificante.
			// Esto ocurre máximo en un individuo de cada 300 generaciones, pudiendo no aparecer por miles de generaciones.
		// TODO de vez en cuando tira null en alguno (pareja o [j2].aplicarMutacion()), revisar los errores y ver si se arreglaron con esto
			if(!elegido1)
				individuo1=poblacionActual[tamañoPoblacion-1];
			if(!elegido2)
				individuo2=poblacionActual[tamañoPoblacion-1];
			
			//crossover
			if(Math.random()>.75){
				nuevaPoblacion[j1]=individuo1;
				nuevaPoblacion[j2]=individuo2;
			}else{
				Individuo[] hijos=individuo1.crossover(individuo2);
				nuevaPoblacion[j1]=hijos[0];
				nuevaPoblacion[j2]=hijos[1];
			}
			
			// Aplicación de mutación.
			nuevaPoblacion[j1].aplicarMutacion();
			nuevaPoblacion[j2].aplicarMutacion();
			
			// cálculo del fitness
			double fitness1=objetivo(nuevaPoblacion[j1])
				,fitness2=objetivo(nuevaPoblacion[j2]);
			
			nuevaPoblacion[j1].fitness=fitness1;
			nuevaPoblacion[j2].fitness=fitness2;
			
			// fitness acumulada de la generación (sirve para la próxima selección)
			sumatoriaPuntuaciones+=fitness1+fitness2;
		}
		
		// Por ahora no, dijo el profe.
		// if(convergencia)
			// break
		// else
			//actualizar poblacionActual
		// La ordenamos para facilitar el cálculo del máximo, mínimo y promedio.
		Arrays.sort(nuevaPoblacion);
		generations.add(nuevaPoblacion);
		poblacionActual=nuevaPoblacion;
		
		calcularMinMaxPro();
		mostrarGeneracion();

		//return nuevaPoblacion;
	}
	
	private void calcularMinMaxPro(){
		
		// maximoIndividuo=minimoIndividuo=null;
		// maximoObjetivo=0;
		// minimoObjetivo=1;
		
		// for(Individuo individuo: poblacionActual){
		// 	//Guardamos los valores para los cálculos posteriores
		// 	double fitness=individuo.fitness;
			
		// 	if(fitness >= maximoObjetivo){
		// 		maximoObjetivo=fitness;
		// 		maximoIndividuo=individuo;
		// 	}
		// 	if(fitness <= minimoObjetivo){
		// 		minimoObjetivo=fitness;
		// 		minimoIndividuo=individuo;
		// 	}
		// }
		
		// TODO revisar que la comparación sea correcta
		maximoIndividuo=poblacionActual[0];
		minimoIndividuo=poblacionActual[tamañoPoblacion-1];
		promedio=sumatoriaPuntuaciones/tamañoPoblacion;
			
		// TODO mostrar los 3
		
	}
	
	private void mostrarGeneracion(){
		System.out.println("Población "+viendoGeneracion+":");
		for(Individuo individuo: poblacionActual){
			System.out.println("\t"+individuo);
		}
		
		// TODO pasar al objeto
		// serieMinimo.add(viendoGeneracion,minimoIndividuo.fitness);
		// seriePromedio.add(viendoGeneracion,promedio);
		// serieMaximo.add(viendoGeneracion,maximoIndividuo.fitness);
		
		// generaciones.setText("Generación "+viendoGeneracion);
	}
	
	private void mostrarGeneracionAnterior(){
		viendoGeneracion--;
		// serieMinimo.remove(viendoGeneracion);
		// seriePromedio.remove(viendoGeneracion);
		// serieMaximo.remove(viendoGeneracion);
		
		// poblacionActual=generations.get(viendoGeneracion-1);
		
		calcularMinMaxPro();
	}
	
	private void mostrarGeneracionExistente(){
		poblacionActual=generations.get(viendoGeneracion-1);
		sumatoriaPuntuaciones=0;
		for(int i=0;i<tamañoPoblacion;i++)
			sumatoriaPuntuaciones+=objetivo(poblacionActual[i]);
		promedio=sumatoriaPuntuaciones/tamañoPoblacion;
		calcularMinMaxPro();
		mostrarGeneracion();
	}

	//API para el frontend

	private void runJS(String code){
		webEngine.executeScript(code);
	}

	public void iniciarSimulacion(int cantidadIndividuos, int tipoSeleccion,boolean conElitismo){
		elitismo=conElitismo;
		seleccionPorRango=tipoSeleccion==2;
		tamañoPoblacion=cantidadIndividuos;
		reiniciar();

		runJS("");
	}

}