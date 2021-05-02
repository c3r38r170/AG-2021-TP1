public class Individuo implements Comparable<Individuo>{
	
	boolean[] cromosoma;
	int valorDecimal=0;
	double fitness;
	
	public Individuo(boolean[] cromosoma){
		this.cromosoma=cromosoma;
		
		for(int i=0;i<30;i++){
			if(cromosoma[i])
				this.valorDecimal+=Math.pow(2,(29-i));
		}
	}
	
	public Individuo[] crossover(Individuo pareja){
		
		// choose a point
		//create 2 cromosomas
		boolean[] cromosomaNuevo1=new boolean[30]
			,cromosomaNuevo2=new boolean[30];
		
		int point=Utils.randomIntBetween(0, 28);//son los 29 puntos entre los 30 genes
		
		for(int i=0;i<=29;i++){
			cromosomaNuevo1[i]=this.cromosoma[i];
			cromosomaNuevo2[i]=pareja.cromosoma[i];
			
			if(i==point){
				boolean[] temp=cromosomaNuevo1;
				cromosomaNuevo1=cromosomaNuevo2;
				cromosomaNuevo2=temp;
			}
		}
		
		return new Individuo[]{new Individuo(cromosomaNuevo1),new Individuo(cromosomaNuevo2)};
	}
	
	public void aplicarMutacion(){
		if(Math.random()<0.05){
			int gen=Utils.randomIntBetween(0, 29);
			// Método de Mutación: invertida
			cromosoma[gen]=!cromosoma[gen];
			
			// Actualizamos el valor decimal.
			double delta=Math.pow(2, 29-gen);
			if(cromosoma[gen]){
				valorDecimal+=delta;
			}else valorDecimal-=delta;
		}
	}
	
	public String cromosomaToString(){
		StringBuilder sb=new StringBuilder();
		for(boolean gen:cromosoma)
			sb.append(gen?"1":"0");
		return sb.toString();
	}
	
	@Override
	public String toString(){
		return "["+cromosomaToString().replace("0", " ")+" | "+valorDecimal+"]";
	}
	
	@Override
	public int compareTo(Individuo otro) {
		return otro.valorDecimal - this.valorDecimal;
	}
	
}
