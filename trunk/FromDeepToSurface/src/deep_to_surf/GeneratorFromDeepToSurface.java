/**
 * 
 */
package deep_to_surf;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import svm_utils.svm_predict;
import svm_utils.svm_train;

import deep_surf_svm_models.ModelBubbleDetection;
import deep_surf_svm_models.ModelLemmaGeneration;


/**
 * @author Miguel Ballesteros
 * Universitat Pompeu Fabra
 *
 */
public class GeneratorFromDeepToSurface {
	
	private long tiempoInicial=0;
	
	private String pathSurface;
	private String pathDeep;
	
	private String pathTestDeep;
	
	HashMap<String,ArrayList<String>> posToWord;
	HashMap<String,ArrayList<ArrayList<String>>> hsBubbles;
	
	ModelBubbleDetection mbd;
	ModelLemmaGeneration mld;
	
	ArrayList<String> listPosDeep;
	ArrayList<String> listPosOnlySurface;
	
	public GeneratorFromDeepToSurface(){
		
	}
	
	public GeneratorFromDeepToSurface(String pathSurface, String pathDeep, String pathTestDeep){
		this.pathSurface=pathSurface;
		this.pathDeep=pathDeep;
		this.pathTestDeep=pathTestDeep;
		ArrayList<CoNLLHash> deepTreebank = CoNLLTreeConstructor.storeTreebank(pathDeep);
		listPosDeep=this.obtainKeys(deepTreebank);
		mbd=new ModelBubbleDetection(listPosDeep);
		
		//MAKE A LIST OF POS THAT ARE OUT OF THE DEEP REPRESENTATION
		//[VB, PP, IN, JJ, CC, RB, DT, VV, VH, NP, NN]
		listPosOnlySurface=new ArrayList<String>();
		listPosOnlySurface.add("VB");listPosOnlySurface.add("PP");listPosOnlySurface.add("IN");listPosOnlySurface.add("JJ");listPosOnlySurface.add("CC");listPosOnlySurface.add("RB");
		listPosOnlySurface.add("DT");listPosOnlySurface.add("VV");listPosOnlySurface.add("VH");listPosOnlySurface.add("NP");listPosOnlySurface.add("NN");
		
		//mld=new ModelLemmaGeneration(listPosOnlySurface); //There must be a method that extracts the list shown above automatically, this is easy.
	}
	
	
	public void training(){
		Date d=new Date();
		tiempoInicial=d.getTime();
		
		System.out.println("Training process started: "+d.toString());

		System.out.print("Detecting bubbles and words, and generating svm models... ");
		
		
		this.generateBubblesAndLemmas();
		d=new Date();
		long tiempoActual=d.getTime();
		long contTiempo=tiempoActual-tiempoInicial;
		//contTiempo=contTiempo/1000;
		tiempoInicial=tiempoActual;
		System.out.println("Done. "+ contTiempo+"ms");
		
		//*****************************************************************************************
		System.out.println("Training LibSvm model for bubble generation... (may take a while) ");
		/*ArrayList<CoNLLHash> deepTreebank = CoNLLTreeConstructor.storeTreebank(pathDeep);
		ArrayList<String> st=this.obtainKeys(deepTreebank);*/
		Iterator<String> it=listPosDeep.iterator();
		while(it.hasNext()){
			String deepPOS=it.next();
			
			String[] args =new String[28];
			args[0]="-s";
			args[1]="0";
			args[2]="-t";
			args[3]="1";
			args[4]="-d";
			args[5]="2";
			args[6]="-g";
			args[7]="0.2";
			args[8]="-r";
			args[9]="0.0";
			args[10]="-n";
			args[11]="0.5";
			args[12]="-n";
			args[13]="0.5";
			args[14]="-m";
			args[15]="100";
			args[16]="-c";
			args[17]="1100.0";
			args[18]="-e";
			args[19]="1.0";
			args[20]="-p";
			args[21]="0.1";
			args[22]="-h";
			args[23]="1";
			args[24]="-b";
			args[25]="0";
			args[26]="-q";
		
			args[27]="bubble_svm_"+deepPOS+".svm";
			try {
				svm_train.main(args);
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		d=new Date();
		tiempoActual=d.getTime();
		contTiempo=tiempoActual-tiempoInicial;
		//contTiempo=contTiempo/1000;
		tiempoInicial=tiempoActual;
		System.out.println("Done. "+ contTiempo+"ms");
		
		
		//************************************************************************************************
		System.out.println("Training LibSvm model for LEMMA generation... (may take a while) ");
		Iterator<String> it2=listPosOnlySurface.iterator();
		while(it2.hasNext()){
			String posOnlySurf=it2.next();
			
			String[] args =new String[28];
			args[0]="-s";
			args[1]="0";
			args[2]="-t";
			args[3]="1";
			args[4]="-d";
			args[5]="2";
			args[6]="-g";
			args[7]="0.2";
			args[8]="-r";
			args[9]="0.0";
			args[10]="-n";
			args[11]="0.5";
			args[12]="-n";
			args[13]="0.5";
			args[14]="-m";
			args[15]="100";
			args[16]="-c";
			args[17]="1100.0";
			args[18]="-e";
			args[19]="1.0";
			args[20]="-p";
			args[21]="0.1";
			args[22]="-h";
			args[23]="1";
			args[24]="-b";
			args[25]="0";
			args[26]="-q";
		
			args[27]="lemma_svm_"+posOnlySurf+".svm";
			try {
				svm_train.main(args);
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		d=new Date();
		tiempoActual=d.getTime();
		contTiempo=tiempoActual-tiempoInicial;
		//contTiempo=contTiempo/1000;
		tiempoInicial=tiempoActual;
		System.out.println("Done. "+ contTiempo+"ms");
		
		//******************************************************************************************************
		
		
		
		//******************************************************************************************************
		/*This part of the code is only to test the N-Classifiers for BUBBLES*/
		System.out.println("Testing (SCRATCH) the bubble classifier (libsvm) loaded... (may take a while) ");
		generateBubblesAndLemmasSCRATCHTESTING();
		it2=listPosDeep.iterator();
		while(it2.hasNext()){
			String deepPOS=it2.next();
			System.out.println("-----------------"+deepPOS+"-----------------");
			String[] args =new String[4];
			args[0]="bubble_svm_test_"+deepPOS+".svm";
			args[1]="bubble_svm_"+deepPOS+".svm.model";
			args[2]="bubble_output_"+deepPOS+".svm";
			//args[0]="-q";
			try {
				svm_predict.main(args);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		d=new Date();
		tiempoActual=d.getTime();
		contTiempo=tiempoActual-tiempoInicial;
		//contTiempo=contTiempo/1000;
		tiempoInicial=tiempoActual;
		System.out.println("Done. "+ contTiempo+"ms");
		
		/*This part of the code is only to test the N-Classifiers for LEMMAS*/
		System.out.println("Testing (SCRATCH) the lemma classifier (libsvm) loaded... (may take a while) ");
		it2=listPosOnlySurface.iterator();
		while(it2.hasNext()){
			String posOnlySurf=it2.next();
			System.out.println("-----------------"+posOnlySurf+"-----------------");
			String[] args =new String[4];
			args[0]="lemma_svm_test_"+posOnlySurf+".svm";
			args[1]="lemma_svm_"+posOnlySurf+".svm.model";
			args[2]="lemma_output_"+posOnlySurf+".svm";
			//args[0]="-q";
			try {
				svm_predict.main(args);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		d=new Date();
		tiempoActual=d.getTime();
		contTiempo=tiempoActual-tiempoInicial;
		//contTiempo=contTiempo/1000;
		tiempoInicial=tiempoActual;
		System.out.println("Done. "+ contTiempo+"ms");
		/*until here*/
		
		//******************************************************************************************************
		
		
		//testingBubbles();
		testingLemmas();
		
	}
	
	public void testingBubbles(){
		//System.out.println(hsBubbles);
		String deepPos="N";
		System.out.println("NOUNS");
		HashMap<String, Integer> errors=new HashMap<String,Integer>();
		try {
			BufferedReader bfo=new BufferedReader(new FileReader("bubble_output_"+deepPos+".svm"));
			BufferedReader bfg=new BufferedReader(new FileReader("bubble_svm_test_"+deepPos+".svm"));
			while(bfo.ready() && bfg.ready()){
				String lineOutput=bfo.readLine();
				String lineGold=bfg.readLine();
				Double outputValue=Double.parseDouble(lineOutput);
				Double goldValue=Double.parseDouble(lineGold.split(" ")[0]);
				
				if (!goldValue.equals(outputValue)){
					//System.out.println("gold:"+goldValue +" predicted:"+outputValue);
					Integer g=goldValue.intValue();
					Integer p=outputValue.intValue();
					//System.out.println("GOLD:"+mbd.getSVMBubble(g.toString())+ " PRED:"+mbd.getSVMBubble(p.toString()));
					String key="GOLD:"+mbd.getSVMBubble(g.toString())+ " PRED:"+mbd.getSVMBubble(p.toString());
					Integer value=errors.get(key);
					if (value==null){
						errors.put(key, 1);
					}
					else {
						value++;
						errors.put(key,value);
					}
					
				}
				
			}
			System.out.println(errors);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testingLemmas(){
		//System.out.println(hsBubbles);
		String pos="IN";
		System.out.println("IN");
		HashMap<String, Integer> errors=new HashMap<String,Integer>();
		try {
			BufferedReader bfo=new BufferedReader(new FileReader("lemma_output_"+pos+".svm"));
			BufferedReader bfg=new BufferedReader(new FileReader("lemma_svm_test_"+pos+".svm"));
			while(bfo.ready() && bfg.ready()){
				String lineOutput=bfo.readLine();
				String lineGold=bfg.readLine();
				Double outputValue=Double.parseDouble(lineOutput);
				Double goldValue=Double.parseDouble(lineGold.split(" ")[0]);
				
				if (!goldValue.equals(outputValue)){
					//System.out.println("gold:"+goldValue +" predicted:"+outputValue);
					Integer g=goldValue.intValue();
					Integer p=outputValue.intValue();
					//System.out.println("GOLD:"+mbd.getSVMBubble(g.toString())+ " PRED:"+mbd.getSVMBubble(p.toString()));
					String key="GOLD:"+mld.getSVMLemma(g.toString())+ " PRED:"+mld.getSVMLemma(p.toString());
					Integer value=errors.get(key);
					if (value==null){
						errors.put(key, 1);
					}
					else {
						value++;
						errors.put(key,value);
					}
					
				}
				
			}
			System.out.println(errors);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
public void generating(){}
	
public void generateBubblesAndLemmasSCRATCHTESTING(){
		
		//System.out.println("Processing the treebank ...");
		
		
		
		ArrayList<CoNLLHash> surfaceTreebank = CoNLLTreeConstructor.storeTreebank("/home/miguel/Desktop/svnTaln/depparsing/Depparsing/FromSurf_to_Deep__From_Deep_to_Surface_Sept2013/FromDeepToSurface/SSyntSpa_V03_NOSEM_ISO_14-02-17_development_set.conll");
		//ArrayList<CoNLLHash> surfaceTreebank = CoNLLTreeConstructor.storeTreebank("/home/miguel/Desktop/svnTaln/depparsing/Depparsing/FromSurf_to_Deep__From_Deep_to_Surface_Sept2013/FromDeepToSurface/SSyntSpa_V03_NOSEM_ISO_14-02-17_test_set.conll");
		ArrayList<CoNLLHash> deepTreebank = CoNLLTreeConstructor.storeTreebank(pathTestDeep);
		
		Iterator<CoNLLHash> itSurfTreebank=surfaceTreebank.iterator();
		Iterator<CoNLLHash> itDeepTreebank=deepTreebank.iterator();
		
		hsBubbles=new HashMap<String,ArrayList<ArrayList<String>>>();
		posToWord=new HashMap<String,ArrayList<String>>();
		
		//ArrayList<String> bubbleKeys=obtainKeys(deepTreebank);
		//System.out.println(bubbleKeys);
		
		int sentenceCounter=1;
		while(itSurfTreebank.hasNext() && itDeepTreebank.hasNext()){
			
			CoNLLHash deepSentence=itDeepTreebank.next();
			CoNLLHash surfaceSentence=itSurfTreebank.next();
			
			ArrayList<String> deepIds=deepSentence.getIds();
			//ArrayList<String> surfaceIds=surfaceSentence.getIds();
			
			
			Iterator<String> itDeepIds=deepIds.iterator();
			while(itDeepIds.hasNext()){
				String id=itDeepIds.next();
				
				String posDeep=CoNLLHash.getSubFeat(deepSentence.getFEAT(id),"dpos");
				if (posDeep.equals("")) posDeep="_";
				//formDeep+="("+sentenceCounter+")";
				ArrayList<String> bubble=new ArrayList<String>();
				
				boolean idxFound=true;
				int cont=0;
				
				ArrayList<String> surfNodes=new ArrayList<String>();
				HashMap<String,String> idToLemma=new HashMap<String,String>();
				HashMap<String,String> idToPos=new HashMap<String,String>();
				while(idxFound){
					String idx="id"+cont;
					String feats=deepSentence.getFEAT(id);
					String surfNode=CoNLLHash.getSubFeat(feats, idx);
					if (!surfNodes.contains(surfNode)){
						surfNodes.add(surfNode);
						//System.out.println(idx);
						if (!surfNode.equals("")) {
							//System.out.println(surfNode);
							if (!surfNode.contains("_") && !surfNode.contains("bis")) {
								//System.out.println(surfNode);
								//System.out.println(surfaceSentence.getIds());
								String pos=surfaceSentence.getPOS(surfNode);
								
								/*StringTokenizer st=new StringTokenizer(surfaceSentence.getFEAT(surfNode));
								//SPOS
								while(st.hasMoreTokens()) {
									String s=st.nextToken("|");
									if (s.contains("spos")) {
										pos=s;
									}
								}*/
							
								if (cont==0){
									pos+="(deep)";
								}
								
								/*if (!pos.contains("(deep)") && pos.equals("VV")){
									System.out.println(sentenceCounter+"--->"+id +": "+deepSentence.getFEAT(id)+"---"+deepSentence.getLemma(id)+" idSuperficie:"+surfNode);
								}*/
								
							    if (!pos.contains("(deep)")){
							    	ArrayList<String> listLemma=posToWord.get(pos);
									if (listLemma==null) listLemma=new ArrayList<String>();
									if (!surfNode.equals("1")){
										listLemma=addLemma(surfaceSentence.getLemma(surfNode),listLemma);
									}
									else {
										listLemma=addLemma(surfaceSentence.getLemma(surfNode).toLowerCase(),listLemma);
									}
								
									
									if (!pos.equals("_")) {
										posToWord.put(pos, listLemma);
									//	if (!bubble.contains(pos)) {
										idToLemma.put(surfNode, surfaceSentence.getLemma(surfNode).toLowerCase());
										idToPos.put(surfNode, surfaceSentence.getPOS(surfNode));
									}
							    }
								
								
								if (!pos.equals("_")) bubble.add(pos);
									//}
							}
						}
						else {
							idxFound=false;
						}
						cont++;
					}
					else {
						cont++;
					}
				}
				
				Iterator<String> itLemma=idToLemma.keySet().iterator();
				while(itLemma.hasNext()){
					String idKey=itLemma.next();
					mld.addLine(idToLemma.get(idKey), bubble.toString(), bubble, id, deepSentence, false, idToPos.get(idKey));
				}
				
				// Generate LIBSVM MODEL WITH THE BUBBLE.
				//bubbleString=bubbleString.toString().replaceAll("]","");
				//Collections.sort(bubble);
				mbd.addLine(bubble.toString(), id, deepSentence, false, posDeep);
				
				
				//if (bubble.size()>1) {
					ArrayList<ArrayList<String>> oldBubble=hsBubbles.get(posDeep);
					if (oldBubble!=null) {
						for (int i=0;i<oldBubble.size();i++){
							if (!oldBubble.contains(bubble)){
								oldBubble.add(bubble);
							}
									
						}
					}
					else {
						oldBubble=new ArrayList<ArrayList<String>>();
						oldBubble.add(bubble);
					}
					hsBubbles.remove(posDeep);
					hsBubbles.put(posDeep, oldBubble);
					
				//}
				
			}
			
			sentenceCounter++;
		}
		//System.out.println(hsBubbles.keySet()); //it should output the dpos nodes.
		//System.out.println(posToWord.keySet());
		//System.out.println(hsBubbles.get("N"));
		//System.out.println(posToWord.get("IN(deep)"));
		
		mbd.closeBuffers(false);
		mld.closeBuffers(false);
		//mbd.closeBuffer(false);
		System.out.println(posToWord);
		
		
	}
	
	
	
	public void generateBubblesAndLemmas(){
		
		//System.out.println("Processing the treebank ...");
		
		
		
		ArrayList<CoNLLHash> surfaceTreebank = CoNLLTreeConstructor.storeTreebank(pathSurface);
		ArrayList<CoNLLHash> deepTreebank = CoNLLTreeConstructor.storeTreebank(pathDeep);
		
		
		Iterator<CoNLLHash> itSurfTreebank=surfaceTreebank.iterator();
		Iterator<CoNLLHash> itDeepTreebank=deepTreebank.iterator();
		
		hsBubbles=new HashMap<String,ArrayList<ArrayList<String>>>();
		posToWord=new HashMap<String,ArrayList<String>>();
		
		//ArrayList<String> bubbleKeys=obtainKeys(deepTreebank);
		//System.out.println(bubbleKeys);
		
		int sentenceCounter=1;
		while(itSurfTreebank.hasNext() && itDeepTreebank.hasNext()){
			
			CoNLLHash deepSentence=itDeepTreebank.next();
			CoNLLHash surfaceSentence=itSurfTreebank.next();
			
			ArrayList<String> deepIds=deepSentence.getIds();
			//ArrayList<String> surfaceIds=surfaceSentence.getIds();
			
			
			Iterator<String> itDeepIds=deepIds.iterator();
			while(itDeepIds.hasNext()){
				String id=itDeepIds.next();
				
				String posDeep=CoNLLHash.getSubFeat(deepSentence.getFEAT(id),"dpos");
				if (posDeep.equals("")) posDeep="_";
				//formDeep+="("+sentenceCounter+")";
				ArrayList<String> bubble=new ArrayList<String>();
				
				boolean idxFound=true;
				int cont=0;
				
				ArrayList<String> surfNodes=new ArrayList<String>();
				HashMap<String,String> idToLemma=new HashMap<String,String>();
				HashMap<String,String> idToPos=new HashMap<String,String>();
				while(idxFound){
					String idx="id"+cont;
					String feats=deepSentence.getFEAT(id);
					String surfNode=CoNLLHash.getSubFeat(feats, idx);
					if (!surfNodes.contains(surfNode)){
						surfNodes.add(surfNode);
						//System.out.println(idx);
						if (!surfNode.equals("")) {
							//System.out.println(surfNode);
							if (!surfNode.contains("_") && !surfNode.contains("bis")) {
								//String pos=surfaceSentence.getPOS(surfNode);
								String pos=surfaceSentence.getPOS(surfNode);
								
								
								if (cont==0){
									pos+="(deep)";
								}
								/*if (!pos.contains("(deep)") && pos.equals("VV")){
									System.out.println(sentenceCounter+"--->"+id +": "+deepSentence.getFEAT(id)+"---"+deepSentence.getLemma(id)+" idSuperficie:"+surfNode);
								}*/
								if (!pos.contains("(deep)")) {
									ArrayList<String> listLemma=posToWord.get(pos);
									if (listLemma==null) listLemma=new ArrayList<String>();
									if (!surfNode.equals("1")){
										listLemma=addLemma(surfaceSentence.getLemma(surfNode),listLemma);
									}
									else {
										listLemma=addLemma(surfaceSentence.getLemma(surfNode).toLowerCase(),listLemma);
									}
									if (!pos.equals("_")) {
										posToWord.put(pos, listLemma);
										idToLemma.put(surfNode, surfaceSentence.getLemma(surfNode).toLowerCase());
										idToPos.put(surfNode, surfaceSentence.getPOS(surfNode));
									}
									//	if (!bubble.contains(pos)) {
									
									
									
								}
								if (!pos.equals("_")) bubble.add(pos);
									//}
							}
						}
						else {
							idxFound=false;
						}
						cont++;
					}
					else {
						cont++;
					}
				}
				Iterator<String> itLemma=idToLemma.keySet().iterator();
				while(itLemma.hasNext()){
					String idKey=itLemma.next();
					mld.addLine(idToLemma.get(idKey), bubble.toString(), bubble, id, deepSentence, true, idToPos.get(idKey));
				}
				//
				
				// Generate LIBSVM MODEL WITH THE BUBBLE.
				//bubbleString=bubbleString.toString().replaceAll("]","");
				//Collections.sort(bubble);
				/*if (!bubble.toString().contains("(deep)") && (bubble.size()>0)) {
					System.out.println("BUBBLE:"+bubble);
				}*/
				mbd.addLine(bubble.toString(), id, deepSentence, true, posDeep);
				
				
				
				//if (bubble.size()>1) {
					ArrayList<ArrayList<String>> oldBubble=hsBubbles.get(posDeep);
					if (oldBubble!=null) {
						for (int i=0;i<oldBubble.size();i++){
							if (!oldBubble.contains(bubble)){
								oldBubble.add(bubble);
							}
									
						}
					}
					else {
						oldBubble=new ArrayList<ArrayList<String>>();
						oldBubble.add(bubble);
					}
					hsBubbles.remove(posDeep);
					hsBubbles.put(posDeep, oldBubble);
				//}
				
				
			}
			
			sentenceCounter++;
		}
		//System.out.println(hsBubbles.keySet()); //it should output the dpos nodes.
		//System.out.println(posToWord.keySet());
		//System.out.println(hsBubbles.get("N"));
		//System.out.println(posToWord.get("IN(deep)"));
		
		mbd.closeBuffers(true);
		mld.closeBuffers(true);
		
		//System.out.println("Bubbles:"+hsBubbles.keySet());
		
		//System.out.println(posToWord);
		//mbd.closeBuffer(false);
		
	}
	
	
	
	private ArrayList<String> obtainKeys(ArrayList<CoNLLHash> deepTreebank) {
		// TODO Auto-generated method stub
		
	    ArrayList<String> out=new ArrayList<String>();
		Iterator<CoNLLHash> itDeepTreebank=deepTreebank.iterator();
		while(itDeepTreebank.hasNext()){
			CoNLLHash deepSentence=itDeepTreebank.next();
			
			ArrayList<String> deepIds=deepSentence.getIds();
			//ArrayList<String> surfaceIds=surfaceSentence.getIds();
			
			
			Iterator<String> itDeepIds=deepIds.iterator();

			while(itDeepIds.hasNext()){
				String id=itDeepIds.next();
			
				String posDeep=CoNLLHash.getSubFeat(deepSentence.getFEAT(id),"dpos");
				if (posDeep.equals("")) posDeep="_";
				if (!out.contains(posDeep)){
					out.add(posDeep); 
				}
			}
		}
		return out;
	}
	
	private ArrayList<String> addLemma(String lemma, ArrayList<String> list) {
		// TODO Auto-generated method stub
		if (!list.contains(lemma)) {
			list.add(lemma);
		}
		return list;
		
	}

public static void main(String [] args) {
		
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("                       Generator 1.0                            ");
		System.out.println("            From Deep Representation to Surface Representation                             ");
		System.out.println("-----------------------------------------------------------------------------");
		//System.out.println("                     Miguel Ballesteros and Leo Wanner                           ");
		//System.out.println("                     @TALN Research Group                             ");
		//System.out.println("                     taln.upf.edu                             ");
		//System.out.println("                     @Pompeu Fabra University                             ");
		//System.out.println("-----------------------------------------------------------------------------");
		
		 
	        /*Option testingOpt = OptionBuilder.withArgName("testing")
	                .hasArg(false)
	                .isRequired(false)
	                .withDescription("testing option")
	                .withLongOpt("testing")
	                .create("p");*/

	        Option ssOpt = OptionBuilder.withArgName("ssynt-treebank")
	                .hasArg(true)
	                .isRequired(false)
	                .withDescription("ssynt treebank")
	                .withLongOpt("tssynt")
	                .create("s");
	        
	        Option dsOpt = OptionBuilder.withArgName("dsynt-treebank")
	                .hasArg(true)
	                .isRequired(false)
	                .withDescription("dsynt treebank")
	                .withLongOpt("tdsynt")
	                .create("d");
	        
	        Option dstTOpt = OptionBuilder.withArgName("dsynt-test")
	                .hasArg(true)
	                .isRequired(false)
	                .withDescription("dsyntest treebank")
	                .withLongOpt("sssyntest")
	                .create("dt");
	        
	        /*Option dsTOpt = OptionBuilder.withArgName("dsynt-test")
	                .hasArg(true)
	                .isRequired(false)
	                .withDescription("dsyntest treebank")
	                .withLongOpt("tdsyntest")
	                .create("dt");*/


			// create Options object
			Options options = new Options();
			//options.addOption(sentenceOpt);
			//options.addOption(testingOpt);
			options.addOption(ssOpt);
			options.addOption(dsOpt);
			options.addOption(dstTOpt);
			//options.addOption(dsTOpt);
			
		
			 // create the command line parser
	        CommandLineParser parser = new BasicParser();
	        try {
	            // parse the command line arguments
	            CommandLine line = parser.parse( options, args );
	            boolean training=true;
	            
	           	GeneratorFromDeepToSurface transducer=new GeneratorFromDeepToSurface(line.getOptionValue("s"),line.getOptionValue("d"), line.getOptionValue("dt"));
	           	//transducer.generateBubblesAndWords();
	           	transducer.training();

	           	
	           	
	             

	            /*String sentenceFilePath = line.getOptionValue("s");
	            String transitionsFilePath = line.getOptionValue("t");
	            String outputFileName = line.getOptionValue("o");
	            
	            Integer sentenceChoice = 0;
	            if (line.hasOption("c")) {
	                sentenceChoice = Integer.parseInt(line.getOptionValue("c")) - 1;
	            }
	            boolean allowRoot = !line.hasOption("nar");
	            boolean verbose = line.hasOption("v");*/
	            
	            //run(sentenceFilePath, transitionsFilePath, outputFileName, allowRoot, sentenceChoice, verbose);
	        }
	        catch( ParseException exp ) {
	            // oops, something went wrong
	            System.err.println(exp.getMessage());
	            
	            // automatically generate the help statement
	            HelpFormatter formatter = new HelpFormatter();
	            formatter.printHelp("Generate through Tree Transducers", options, true);
	       }
			
		//(if (args)
		
		//transducer.training();
		
		//
		//transducer.test();
		
	}

}
