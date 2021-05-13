import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class App extends JFrame {
	
	private static final long serialVersionUID = 1L;

	private boolean elitismo=false;
	private boolean seleccionPorRango=false;
	
	private Individuo[] poblacionActual;
	
	private Individuo maximoIndividuo;
	private Individuo minimoIndividuo;
	private double promedio;
	private double sumatoriaPuntuaciones=0;
	
	private int tamañoPoblacion=0;
	
	private WebEngine webEngine;
 
  // Función que sirve para darle el fitness a cada individuo.
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
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

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

		// Preparación de la ventana.
		setIconImage(new ImageIcon(getClass().getResource("/res/Logo AG.png")).getImage());
		setTitle("Algoritmos Genéticos - TP1");
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width*3/4,screenSize.height*3/4);
		setLocation(screenSize.width/8, screenSize.height/8);
		setVisible(true);
	}
	
  // Función para iniciar la simulación.
	private void reiniciar(){
		sumatoriaPuntuaciones=0;
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
		
		ordenarPoblacion(poblacionActual);
		calcularMinMaxPro();
	}
	
  // Función para realizar cada generación de la simulación.
	private void nuevaGeneracion(){
		Individuo[] nuevaPoblacion=new Individuo[tamañoPoblacion];
		int cantidadPares=tamañoPoblacion/2;
		
		double[] vectorProbabilidades=new double[tamañoPoblacion];
		Consumer<Integer> calculoDeProbabilidades=seleccionPorRango?
			// En la selección por rango, la probabilidad de crossover se basa en el rango (1ro, 2do, 3ro...) del individuo en la población.
			j->vectorProbabilidades[j]=(1.2-.4* /* j es el rango (0 es el más alto) -> */ j /* <- */ /(tamañoPoblacion-1))/tamañoPoblacion
			// En la selección por ruleta, usamos el fitness para calcular las probabilidades de crossover.
			:j->vectorProbabilidades[j]=poblacionActual[j].fitness/sumatoriaPuntuaciones
		;
		for(int j=0;j<tamañoPoblacion;j++)
			calculoDeProbabilidades.accept(j);
		
		sumatoriaPuntuaciones=0;
		
		if(elitismo){
			// Reemplazamos el último par por los mejores individuos. (Recuerde que las poblaciones están ordenadas.)
			cantidadPares--;
			nuevaPoblacion[tamañoPoblacion-1]=poblacionActual[0];
			nuevaPoblacion[tamañoPoblacion-2]=poblacionActual[1];
			sumatoriaPuntuaciones=poblacionActual[0].fitness+poblacionActual[1].fitness;
		}
			
		// Aplicación de selección.
		for(int j=0;j<cantidadPares;j++){
			int j1=j*2,j2=j1+1;
			boolean elegido1=false
				,elegido2=false;
			double selector1=Math.random()
				,selector2=Math.random()
				,acc=0;
			Individuo individuo1=null
				,individuo2=null;
			// Los nulls son para que no me tire la advertencia "The local variable may not have been initialized" más abajo en la IDE.
			
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
			// Esto ocurrió en nuestras simulaciones como máximo en un individuo de cada 300 generaciones, pudiendo no aparecer por miles de generaciones.
		// TODO de vez en cuando tira null en alguno (pareja o [j2].aplicarMutacion()), revisar los errores y ver si se arreglaron con esto
			if(!elegido1)
				individuo1=poblacionActual[tamañoPoblacion-1];
			if(!elegido2)
				individuo2=poblacionActual[tamañoPoblacion-1];
			
			// Aplicación de crossover. (Se encarga la clase Individuo)
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
			
			// Cálculo del fitness. (Ver método objetivo.)
			double fitness1=objetivo(nuevaPoblacion[j1])
				,fitness2=objetivo(nuevaPoblacion[j2]);
			
			nuevaPoblacion[j1].fitness=fitness1;
			nuevaPoblacion[j2].fitness=fitness2;
			
			// Fitness acumulada de la generación (sirve para la próxima selección);
			sumatoriaPuntuaciones+=fitness1+fitness2;
		}
		
		// Por ahora no, dijo el profe.
		// if(convergencia)
			// break
		// else
			//actualizar poblacionActual

		ordenarPoblacion(nuevaPoblacion);
		poblacionActual=nuevaPoblacion;
		
		calcularMinMaxPro();
	}

	// Ordenamos la población para facilitar el cálculo del máximo, mínimo y promedio.
	private void ordenarPoblacion(Individuo[] poblacion){
		Arrays.sort(poblacion);
	}

  // Función que calcula Mínimo, Máximo y Promedio de cada generación.
  // Solo calcula el promedio, ya que los individuos se encuentran ordenados en la población.
	private void calcularMinMaxPro(){
		maximoIndividuo=poblacionActual[0];
		minimoIndividuo=poblacionActual[tamañoPoblacion-1];
		promedio=sumatoriaPuntuaciones/tamañoPoblacion;
	}

	// API para el frontend.

	private void mandarGeneracionActual(){
		StringBuilder JSCommand=new StringBuilder("proximaGeneracion({min:"+minimoIndividuo.fitness+",pro:"+promedio+",max:"+maximoIndividuo.fitness+",individuos:[");
		
		String[] poblacionAsJSON=new String[tamañoPoblacion];
		for (int i = 0; i < tamañoPoblacion; i++)
			poblacionAsJSON[i]=poblacionActual[i].toJSONObject();
		
		JSCommand.append(String.join(",",poblacionAsJSON)+"]});");
		webEngine.executeScript(JSCommand.toString());
	}

	public void iniciarSimulacion(int cantidadIndividuos, int tipoSeleccion,boolean conElitismo){
		tamañoPoblacion=cantidadIndividuos%2==0?
			cantidadIndividuos
			:cantidadIndividuos-1;
		seleccionPorRango=tipoSeleccion==2;
		elitismo=conElitismo;
		reiniciar();

		mandarGeneracionActual();
	}

	public void siguienteGeneracion(){
		nuevaGeneracion();
		mandarGeneracionActual();
	}

}