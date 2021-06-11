import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Arrays;

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
	private double[] vectorFitness;
	
	private Individuo maximoIndividuo;
	private Individuo minimoIndividuo;
	private double promedio;
	private double sumatoriaPuntuaciones=0;
	
	private int tamañoPoblacion=0;
	
	private WebEngine webEngine;

  // Función que sirve para evaluar el desempeño de cada individuo.
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
			poblacionActual[i].valorFuncionObjetivo=fitness;
			sumatoriaPuntuaciones+=fitness;
		}
		
		ordenarPoblacion(poblacionActual);
		calcularMinMaxPro();
	}
	
  // Función para realizar cada generación de la simulación.
	private void nuevaGeneracion(){
		
		vectorFitness=new double[tamañoPoblacion];
		for(int j=0;j<tamañoPoblacion;j++)
			vectorFitness[j]=poblacionActual[j].valorFuncionObjetivo/sumatoriaPuntuaciones;
		
		sumatoriaPuntuaciones=0;

		Individuo[] nuevaPoblacion=seleccionPorRango?
			nuevaGeneracionPorRango()
			:nuevaGeneracionPorRuleta();
		
		// Por ahora no, dijo el profe.
		// if(convergencia)
			// break
		// else
			//actualizar poblacionActual

		ordenarPoblacion(nuevaPoblacion);
		poblacionActual=nuevaPoblacion;
		
		calcularMinMaxPro();
	}

	private Individuo[] nuevaGeneracionPorRuleta(){
		int cantidadPares=tamañoPoblacion/2;
		Individuo[] nuevaPoblacion=new Individuo[tamañoPoblacion];

		if(elitismo){
			// Reemplazamos el último par por los mejores individuos. (Recuerde que las poblaciones están ordenadas.)
			cantidadPares--;
			nuevaPoblacion[tamañoPoblacion-1]=poblacionActual[0].crearClon();
			nuevaPoblacion[tamañoPoblacion-2]=poblacionActual[1].crearClon();
			sumatoriaPuntuaciones=poblacionActual[0].valorFuncionObjetivo+poblacionActual[1].valorFuncionObjetivo;
		}
			
		for(int j=0;j<cantidadPares;j++){
			
			// Aplicación de selección.
			int j1=j*2,j2=j1+1;
			Individuo individuo1=poblacionActual[elegirIndicePorRuleta(vectorFitness)].crearClon()
				,individuo2=poblacionActual[elegirIndicePorRuleta(vectorFitness)].crearClon();
	
			// Aplicación de crossover. (Se encarga la clase Individuo)
			if(individuo1.equals(individuo2) || Math.random()>.75){
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
			
			// Cálculo del valor objetivo. (Ver método objetivo.)
			double valorObjetivo1=objetivo(nuevaPoblacion[j1])
				,valorObjetivo2=objetivo(nuevaPoblacion[j2]);
			
			nuevaPoblacion[j1].valorFuncionObjetivo=valorObjetivo1;
			nuevaPoblacion[j2].valorFuncionObjetivo=valorObjetivo2;
			
			// Sumatoria de todos los resultados de la función objetivo de la generación (sirve para el promedio y la próxima selección).
			sumatoriaPuntuaciones+=valorObjetivo1+valorObjetivo2;
		}

		return nuevaPoblacion;
	}
	
	private Individuo[] nuevaGeneracionPorRango(){
		Individuo[] nuevaPoblacion=new Individuo[tamañoPoblacion];

		// Elegimos un M de individuos aleatorio, mayor o igual a 1, menor o igual a la mitad de individuos.
		// Y pasamos todos los individuos menos los M que reemplazaremos por descendencia de los mejores M.
		int m=Utils.randomIntBetween(1, tamañoPoblacion/2);
		for(int i=0,til=tamañoPoblacion-m*2;i<til;i++){
			Individuo delMedio=poblacionActual[i+m].crearClon();
			// TODO aplicar 
			nuevaPoblacion[tamañoPoblacion-i-1]=delMedio;
			sumatoriaPuntuaciones+=delMedio.valorFuncionObjetivo;
		}
			
		for(int j=0;j<m;j++){
			
			// Aplicación de selección.
			int j1=j*2,j2=j1+1;
			Individuo individuo1=poblacionActual[elegirIndicePorRuleta(vectorFitness,j)].crearClon()
				,individuo2=poblacionActual[j].crearClon();
			nuevaPoblacion[j2]=individuo2;
	
			// Aplicación de crossover. (Se encarga la clase Individuo)
			if(Math.random()<.25)
				nuevaPoblacion[j1]=individuo1;
			else{ 
        // Elegimos un hijo arbitrariamente.
				// TODO preguntar con qué hijo me quedo
				nuevaPoblacion[j1]=individuo1.crossover(individuo2)[(int)Math.round(Math.random())];
				nuevaPoblacion[j1].valorFuncionObjetivo=objetivo(nuevaPoblacion[j1]);
			}
			
			// Aplicación de mutación.
			if(nuevaPoblacion[j1].aplicarMutacion())
				nuevaPoblacion[j1].valorFuncionObjetivo=objetivo(nuevaPoblacion[j1]);
			
			// Sumatoria de todos los resultados de la función objetivo de la generación (sirve para el promedio y la próxima selección).
			sumatoriaPuntuaciones+=nuevaPoblacion[j1].valorFuncionObjetivo+individuo2.valorFuncionObjetivo;
		}

		return nuevaPoblacion;
	}

	private int elegirIndicePorRuleta(double[] vectorFitness, int evitar) {
		// En este método evitamos uno de los índices.
		double totalSumaVector=0;
		for(int i=0,til=vectorFitness.length;i<til;i++){
			if(i==evitar)
				continue;
			totalSumaVector+=vectorFitness[i];
		}
		totalSumaVector=Math.random()*totalSumaVector;

		vectorFitness[evitar]=vectorFitness[vectorFitness.length-1];
		return elegirIndicePorRuleta(Arrays.copyOf(vectorFitness, vectorFitness.length-1),totalSumaVector);
	}
	
	private int elegirIndicePorRuleta(double[] vectorFitness){
		return elegirIndicePorRuleta(vectorFitness, 1.0);
	}

	private int elegirIndicePorRuleta(double[] vectorFitness, double totalSumaVector){
		double acc=0,selector=Math.random()*totalSumaVector;
		// No hay forma de que la probabilidad (selector) sea mayor a 1, y la suma (acc) va a llegar a 1 en algun momento.
		// Por lo que este for va a en algún momento terminar con un elegido.
		for(int l=0;l<vectorFitness.length;l++){
			acc+=vectorFitness[l];
			if(acc>selector)
				return l;
		}
		// Aún así, a veces por división de punto flotante, la suma no es exactamente igual a 1 y el número aleatorio puede entrar en ese margen de error.
		// Por lo que en ese caso, elegimos el último.
		// Técnicamente le estamos asignando el resto de la probabilidad a un cromosoma aleatorio, pero es una probabilidad insignificante.
		// Esto ocurrió en nuestras simulaciones como máximo en un individuo de cada 600 generaciones, pudiendo no aparecer por miles de generaciones.
		return vectorFitness.length-1;
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
		StringBuilder JSCommand=new StringBuilder("proximaGeneracion({min:"+minimoIndividuo.valorFuncionObjetivo+",pro:"+promedio+",max:"+maximoIndividuo.valorFuncionObjetivo+",individuos:[");
		
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