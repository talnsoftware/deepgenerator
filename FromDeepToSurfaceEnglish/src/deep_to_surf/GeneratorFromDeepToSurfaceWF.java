/**
 * 
 */
package deep_to_surf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
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
import deep_surf_svm_models.ModelBubbleExternalAttachments;
import deep_surf_svm_models.ModelBubbleInternalAttachments;
import deep_surf_svm_models.ModelLemmaGeneration;


/**
 * @author Miguel Ballesteros
 * Universitat Pompeu Fabra
 *
 */
public class GeneratorFromDeepToSurfaceWF {
	
	private long tiempoInicial=0;
	
	private String pathSurface;
	private String pathDeep;
	
	private String pathTestDeep;
	
	HashMap<String,ArrayList<String>> posToWord;
	HashMap<String,ArrayList<ArrayList<String>>> hsBubbles;
	
	ModelBubbleDetection mbd;
	ModelLemmaGeneration mld;
	ModelBubbleInternalAttachments mbia;
	ModelBubbleExternalAttachments mbea;
	
	ArrayList<String> listPosDeep;
	ArrayList<String> listPosOnlySurface;
	
	ArrayList<String> bubbles2More;
	
	public GeneratorFromDeepToSurfaceWF(){
		
	}
	
	public GeneratorFromDeepToSurfaceWF(String pathSurface, String pathDeep, String pathTestDeep){
		this.pathSurface=pathSurface;
		this.pathDeep=pathDeep;
		this.pathTestDeep=pathTestDeep;
		ArrayList<CoNLLHash> deepTreebank = CoNLLTreeConstructor.storeTreebank(pathDeep);
		listPosDeep=this.obtainKeys(deepTreebank);
		mbd=new ModelBubbleDetection(listPosDeep);
		
		mld=new ModelLemmaGeneration(); //There must be a method that extracts the list shown above automatically, this is easy. But so far, it is hard-coded!!!!!!!! CHANGE.
		mbia=new ModelBubbleInternalAttachments();
		mbea=new ModelBubbleExternalAttachments();
		
		bubbles2More=new ArrayList<String>();
	}
	
	
	public void training(){
		Date d=new Date();
		tiempoInicial=d.getTime();
		
		System.out.println("Training process started: "+d.toString());

		System.out.print("Detecting bubbles and words, and generating svm models... ");
		
		
		this.generateBubblesAndLemmasForTraining();
		d=new Date();
		long tiempoActual=d.getTime();
		long contTiempo=tiempoActual-tiempoInicial;
		//contTiempo=contTiempo/1000;
		tiempoInicial=tiempoActual;
		System.out.println("Done. "+ contTiempo+"ms");
		
		//*****************************************************************************************
		System.out.println("Training LibSvm model for BUBBLE generation... (may take a while) ");
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
			/*try {
				svm_train.main(args);
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		d=new Date();
		tiempoActual=d.getTime();
		contTiempo=tiempoActual-tiempoInicial;
		//contTiempo=contTiempo/1000;
		tiempoInicial=tiempoActual;
		System.out.println("Done. "+ contTiempo+"ms");
		
		
		//************************************************************************************************
		System.out.println("Training LibSvm model for LEMMA generation... (may take a while) ");
		Iterator<String> it2=this.mld.getClassifiers().iterator();
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
			/*try {
				svm_train.main(args);
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		d=new Date();
		tiempoActual=d.getTime();
		contTiempo=tiempoActual-tiempoInicial;
		//contTiempo=contTiempo/1000;
		tiempoInicial=tiempoActual;
		System.out.println("Done. "+ contTiempo+"ms");
		
		
		//************************************************************************************************
		System.out.println("Training LibSvm model for INTERNAL ATTACHMENTS generation... (may take a while) ");
		Iterator<String> it3=mbia.getClassifiers().iterator();
		while(it3.hasNext()){
			String bubbleType=it3.next();
					
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
			args[17]="500.0";
			args[18]="-e";
			args[19]="1.0";
			args[20]="-p";
			args[21]="0.1";
			args[22]="-h";
			args[23]="1";
			args[24]="-b";
			args[25]="0";
			args[26]="-q";
			
			HashMap<String,Integer> mapping=mbia.getMappingClassifiers();
			args[27]="iattachment_svm_"+mapping.get(bubbleType)+".svm";
			/*try {
				svm_train.main(args);
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		d=new Date();
		tiempoActual=d.getTime();
		contTiempo=tiempoActual-tiempoInicial;
		//contTiempo=contTiempo/1000;
		tiempoInicial=tiempoActual;
		System.out.println("Done. "+ contTiempo+"ms");
					
					
		//************************************************************************************************
		System.out.println("Training LibSvm model for EXTERNAL ATTACHMENTS generation... (may take a while) ");
		Iterator<String> it4=mbea.getClassifiers().iterator();
		while(it4.hasNext()){
			String bubbleType=it4.next();
					
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
			args[17]="500.0";
			args[18]="-e";
			args[19]="1.0";
			args[20]="-p";
			args[21]="0.1";
			args[22]="-h";
			args[23]="1";
			args[24]="-b";
			args[25]="0";
			args[26]="-q";
				
			HashMap<String,Integer> mapping=mbea.getMappingClassifiers();
			args[27]="eAttachment_svm_"+mapping.get(bubbleType)+".svm";
			/*try {
				svm_train.main(args);
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}*/
		}
		d=new Date();
		tiempoActual=d.getTime();
		contTiempo=tiempoActual-tiempoInicial;
		//contTiempo=contTiempo/1000;
		tiempoInicial=tiempoActual;
		System.out.println("Done. "+ contTiempo+"ms");
				
		//******************************************************************************************************
		System.out.println("Generating output and dependency tree... (may take a longer while)");
		generateBubblesAndLemmasForTesting();
		System.out.println();
		//*******************************************************************************************************
		d=new Date();
		tiempoActual=d.getTime();
		contTiempo=tiempoActual-tiempoInicial;
		//contTiempo=contTiempo/1000;
		tiempoInicial=tiempoActual;
		System.out.println("Done. "+ contTiempo+"ms");	
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
	
public void generateBubblesAndLemmasForTesting(){
		
		
		ArrayList<CoNLLHash> deepTestTreebank = CoNLLTreeConstructor.storeTreebank(pathTestDeep);
		
		Iterator<CoNLLHash> itDeepTreebank=deepTestTreebank.iterator();
		
		hsBubbles=new HashMap<String,ArrayList<ArrayList<String>>>();
		posToWord=new HashMap<String,ArrayList<String>>();
		
		//ArrayList<String> bubbleKeys=obtainKeys(deepTreebank);
		//System.out.println(bubbleKeys);
		
			int sentenceCounter=1;
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter("output.txt"));
			
			while(itDeepTreebank.hasNext()){
				if (sentenceCounter % 5==0) System.out.print(sentenceCounter+"  ");
				if (sentenceCounter % 100==0) System.out.println();
				sentenceCounter++;
				//System.out.println("--------------------------------------------------");
				
				CoNLLHash deepSentence=itDeepTreebank.next();
				
				
				ArrayList<String> deepIds=deepSentence.getIds();
				//ArrayList<String> surfaceIds=surfaceSentence.getIds();

					Iterator<String> itDeepIds=deepIds.iterator();
					int index=1;
					HashMap<String,String> idDeepToBubble=new HashMap<String,String>();
					HashMap<String,String> surfIdToWords=new HashMap<String,String>();
					HashMap<String,String> surfIdToPos=new HashMap<String,String>();
					HashMap<String,String> deepIdToSurfIds=new HashMap<String,String>();
					HashMap<String,ArrayList<String>> deepIdToNumberedBubble=new HashMap<String,ArrayList<String>>();
					HashMap<String,String> surfIdToDeepIds=new HashMap<String,String>();
					HashMap<String,ArrayList<String>> deepToSurfNodes=new HashMap<String,ArrayList<String>>();
					
					HashMap<String,String> deepIdToBubbleGovernor=new HashMap<String,String>();
					
					HashMap<String,String> surfIdToDeprel=new HashMap<String,String>();
					HashMap<String,String> surfIdToHead=new HashMap<String,String>();
					
					
					HashMap<String,String> surfIdToFeats=new HashMap<String,String>();
					
					
					//HashMap<String,ArrayList<String>> deepToSurfNodes=new HashMap<String,ArrayList<String>>();
					while(itDeepIds.hasNext()){
						String id=itDeepIds.next();
						
						
						
						
						String posDeep=CoNLLHash.getSubFeat(deepSentence.getFEAT(id),"spos");
						if (posDeep.equals("")) posDeep="_";
						
						mbd.generateTestingFile(posDeep);
						mbd.addLine("?", id, deepSentence, false, posDeep);
						mbd.closeBuffers(false);
						
						String[] args =new String[4];
						args[1]="bubble_svm_test_"+posDeep+".svm";
						args[2]="bubble_svm_"+posDeep+".svm.model";
						args[3]="bubble_output_"+posDeep+".svm";
						args[0]="-q"; //quiet mode
						try {
							svm_predict.main(args);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String bubble=mbd.getSVMBubbleSingleFile("bubble_output_"+posDeep+".svm");		
						//System.out.println(bubble);
						idDeepToBubble.put(id, bubble);
						
						String auxBubble=bubble.replaceAll("]","");
						ArrayList<String> surfNodes=new ArrayList<String>();
						
						ArrayList<String> surfLemmas=new ArrayList<String>();
						ArrayList<String> bubbleArray=new ArrayList<String>();
						
						StringTokenizer st=new StringTokenizer(auxBubble,",");
						while(st.hasMoreElements()){
							String s=st.nextToken();
							s=s.replace(" ", "");
							String aux=s.replaceAll("\\[","");
							aux=aux.replaceAll("\\]", "");
							bubbleArray.add(aux);
							if (s.contains("(deep)")){
								//String lemma=deepSentence.getLemma(id);
								String lemma=deepSentence.getForm(id);
								String feats=deepSentence.getFEAT(id);
								if (feats.contains("spos=relative_pronoun")){
									lemma="que";
								}
								if (feats.contains("spos=pronoun") && feats.contains("coref_id=")){
									lemma="él";
								}
								if (lemma.equals("si_mismo")){
									lemma="se";
								}
								surfLemmas.add(lemma);
								
								//HERE WE SHOULD GENERATE THE LINE IN THE CONLL FILE
								//System.out.println(lemma);
								deepIdToSurfIds.put(id+"", index+"");
								surfIdToDeepIds.put(index+"", id+"");
								surfIdToWords.put(index+"", lemma);
								surfIdToPos.put(index+"", deepSentence.getPOS(id));
								surfNodes.add(index+"");
								index++;
	
							}
							else if (!s.equals("") && !s.equals("[")){
								mld.generateTestingFile(s);
								mld.addLine("?", bubble, null, id, deepSentence, false, s);
								mld.closeBuffers(false);
								
								String[] args2 =new String[4];
								//System.out.println(s);
								args2[1]="lemma_svm_test_"+s+".svm";
								args2[2]="lemma_svm_"+s+".svm.model";
								args2[3]="lemma_output_"+s+".svm";
								args2[0]="-q"; //quiet mode
								try {
									svm_predict.main(args2);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								String lemma=mld.getSVMLemmaSingleFile("lemma_output_"+s+".svm");
								surfLemmas.add(lemma);
								surfIdToWords.put(index+"", lemma);
								surfIdToPos.put(index+"", s);
								surfNodes.add(index+"");
								index++;
								
								//System.out.println(lemma);
								
								//now we have the lemmas and the bubbles.  We should generate the internal attachments first, and then the external attachments. We already have the models trained.
							}
							//System.out.println(surfNodes);
							//System.out.println(bubble);
						}
						//System.out.println(bubbleArray);
						//*********************************************
						//internal attachments in this loop
						ArrayList<String> numberedBubble=new ArrayList<String>();
						numberedBubble.addAll(bubbleArray);
						String bubbleGovernor="";
						if (bubbleArray.size()>1){
							mbia.generateTestingFile(bubble);
							//public void addLine(String targiAttachments, ArrayList<String> lexFeatures, ArrayList<String> bubbleArray, String deepId, CoNLLHash deepSentence, boolean train, String bubble) {
							mbia.addLine("?",surfLemmas, bubbleArray, id, deepSentence, false, bubble.toString());
							mbia.closeBuffers(false);
							
							
							HashMap<String,Integer> mapping=mbia.getMappingClassifiers();

							String linksString="";
							
							//System.out.println(bubble);
							
							if (!bubble.equals("[]") && mapping.get(bubble)!=null){

								String[] args3 =new String[4];
								//System.out.println(s);
								args3[1]="iattachment_svm_test_"+mapping.get(bubble)+".svm";
								args3[2]="iattachment_svm_"+mapping.get(bubble)+".svm.model";
								args3[3]="iattachment_output_"+mapping.get(bubble)+".svm";
								args3[0]="-q"; //quiet mode
								try {
									svm_predict.main(args3);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								linksString=mbia.getSVMiAttachmentsSingleFile("iattachment_output_"+mapping.get(bubble)+".svm");
								//System.out.println(internalAttachments);
								StringTokenizer stLinks=new StringTokenizer(linksString,",");
								//System.out.println(linksString);
								//PROCESARLOS!!! HAY QUE GENERAR LOS HEADS....
								
								numberedBubble=new ArrayList<String>();
								
								linksString=linksString.replaceAll("\\[","").replaceAll("\\]","");
								
								while(stLinks.hasMoreTokens()){
									String linkS=stLinks.nextToken();
									String[] spLinks=null;
									if (linkS.contains("->")){
										spLinks=linkS.split("->");
									}
									else if (linkS.contains("<-")){
										spLinks=linkS.split("<-");
									}
									try{
										String splinks0=spLinks[0].replaceAll(" ", "").replaceAll("\\[","").replaceAll("\\]","");
										String splinks1=spLinks[1].replaceAll(" ", "").replaceAll("\\[","").replaceAll("\\]","");
										if (!numberedBubble.contains(splinks0)) numberedBubble.add(splinks0);
										if (!numberedBubble.contains(splinks1.split(":")[0]))numberedBubble.add(splinks1.split(":")[0]);
									}catch(Exception e){
										
									}
								}
							} else if (mapping.get(bubble)==null){
								//default attachments!!
								StringTokenizer stBub=new StringTokenizer(bubble);
								ArrayList<String> nodes=new ArrayList<String>();
								String deep="";
								while(stBub.hasMoreTokens()){
									String s=stBub.nextToken(",");
									s=s.replaceAll(" ", "").replaceAll("\\[","").replaceAll("\\]","");
									nodes.add(s);
									if (s.contains("deep")) deep=s;
								}
								linksString="";
								ArrayList<String> linksArray=new ArrayList<String>();
								int count=0;
								for (int i=0;i<nodes.size();i++){
									String node=nodes.get(i);
									if (!node.contains("deep")){
										String newLink=deep+"<-"+node+"_"+count+":rel("+"DEP"+")"; //by default
										linksArray.add(newLink);
										count++;
									}
									
								}
								linksString=linksArray.toString();
								
								numberedBubble=new ArrayList<String>();
								
								linksString=linksString.replaceAll("\\[","").replaceAll("\\]","");
								StringTokenizer stLinks=new StringTokenizer(linksString,",");
								
								while(stLinks.hasMoreTokens()){
									String linkS=stLinks.nextToken();
									String[] spLinks=null;
									if (linkS.contains("->")){
										spLinks=linkS.split("->");
									}
									else if (linkS.contains("<-")){
										spLinks=linkS.split("<-");
									}
									try{
										String splinks0=spLinks[0].replaceAll(" ", "").replaceAll("\\[","").replaceAll("\\]","");
										String splinks1=spLinks[1].replaceAll(" ", "").replaceAll("\\[","").replaceAll("\\]","");
										if (!numberedBubble.contains(splinks0)) numberedBubble.add(splinks0);
										if (!numberedBubble.contains(splinks1.split(":")[0]))numberedBubble.add(splinks1.split(":")[0]);
									}catch(Exception e){
										
									}
								}

							}
							
							//System.out.println(linksString);
							
							StringTokenizer stLinks=new StringTokenizer(linksString,",");
							
							ArrayList<String> internals=new ArrayList<String>();
							
							//System.out.println(surfNodes);
							
							while(stLinks.hasMoreTokens() && !linksString.isEmpty()){
								String link=stLinks.nextToken(",");
								//System.out.println(link);
								//***************************************************
								//Here I should get the internal attachments... just use indexOf (bubble) y surfNodes. Check whether this is okay. but it seems.
								if (link.contains("->")){
									String[] spLinks=link.split("->");
									internals.add(spLinks[1].split(":")[0].replaceAll(" ", "").replaceAll("\\[", "").replaceAll("\\]", ""));
									
									//there is a link between the word to the left and the word to the right. The word to the left is the head.
									String head=spLinks[0].replaceAll(" ", "").replaceAll("\\[", "").replaceAll("\\]", "");
									String child=spLinks[1].split(":")[0].replaceAll(" ", "").replaceAll("\\[", "").replaceAll("\\]", "");
									String label=spLinks[1].split(":")[1].replaceAll(" ", "").replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("rel\\(","").replaceAll("\\)","");
									
									/*System.out.println(link);
									System.out.println(head);
									System.out.println(child);
									System.out.println(label);
									
									String lemmaHead=surfIdToWords.get(surfNodes.get(numberedBubble.indexOf(head)));
									System.out.println(lemmaHead);*/
									
									//HashMap<String,String> surfIdToDeprel=new HashMap<String,String>();
									//HashMap<String,String> surfIdToHead=new HashMap<String,String>();
									
									surfIdToDeprel.put(surfNodes.get(numberedBubble.indexOf(child)), label);
									surfIdToHead.put(surfNodes.get(numberedBubble.indexOf(child)), surfNodes.get(numberedBubble.indexOf(head)));
									
									//System.out.println("--------------------------------");
									
									
								}
								else if (link.contains("<-")){
									String[] spLinks=link.split("<-");
									internals.add(spLinks[0].split(":")[0].replaceAll(" ", "").replaceAll("\\[", "").replaceAll("\\]", ""));
									
									//there is a link between the word to the left and the word to the right. The word to the right is the head.
									String child=spLinks[0].replaceAll(" ", "").replaceAll("\\[", "").replaceAll("\\]", "");
									String head=spLinks[1].split(":")[0].replaceAll(" ", "").replaceAll("\\[", "").replaceAll("\\]", "");
									//System.out.println(link);
									String label=spLinks[1].split(":")[1].replaceAll(" ", "").replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("rel\\(","").replaceAll("\\)","");
									
									/*System.out.println(link);
									System.out.println(head);
									System.out.println(child);
									System.out.println(label);*/
									try {
									surfIdToDeprel.put(surfNodes.get(numberedBubble.indexOf(child)), label);
									surfIdToHead.put(surfNodes.get(numberedBubble.indexOf(child)), surfNodes.get(numberedBubble.indexOf(head)));
									}catch(Exception e){
										
									}
									
									//System.out.println("--------------------------------");
									
									
									
								}
							}
							
							//System.out.println("Internals:"+internals);
							String bubbleToProcs=numberedBubble.toString();
							bubbleToProcs=bubbleToProcs.replaceAll("\\[","");
							bubbleToProcs=bubbleToProcs.replaceAll("\\]","");
							bubbleToProcs=bubbleToProcs.replaceAll(" ","");
							
							StringTokenizer st2=new StringTokenizer(bubbleToProcs,",");
							//System.out.println(bubbleToProcs);
							int l=0;
							while(st2.hasMoreTokens()){
								String posS=st2.nextToken(",");
								
								if (!internals.contains(posS)){
									bubbleGovernor=posS;
								}
								
							}

							
						}
						else if (!bubble.isEmpty()){ //very easy in the case of a single node
							bubbleGovernor=bubbleArray.get(0);
							//numberedBubble.addAll(bubbleArray);
						}
						//just to be safe
						if (numberedBubble.isEmpty() && !bubble.isEmpty()){
							bubbleGovernor=bubbleArray.get(0);
							numberedBubble.addAll(bubbleArray);
						}
						
						//YA TENGO EL BUBBLE GOVERNOR!!!
						deepIdToBubbleGovernor.put(id,bubbleGovernor);
						deepIdToNumberedBubble.put(id, numberedBubble);
						
						//System.out.println("BUBBLE:"+numberedBubble);
						//System.out.println(bubbleGovernor); //HAY VECES QUE ES EMPTY!! No es problema. Es por los nodos coref, esos tienen que desaparecer en surface.
						//System.out.println("--------------------------");

						//System.out.println(surfNodes);
						//deepToSurfNodes.put(deepIdToSurfIds.get(id),surfNodes);
						deepToSurfNodes.put(id,surfNodes);
						
					}
					
					//------------------------------------------------------------------------------------------------------------------------------------------------------
					//*****************************************************************************************************************************************************
					//EXTERNAL ATTACHMENTS
					
					/*HashMap<String,String> idDeepToBubble=new HashMap<String,String>();
					HashMap<String,String> surfIdToWords=new HashMap<String,String>();
					HashMap<String,String> surfIdToPos=new HashMap<String,String>();
					HashMap<String,String> deepIdToSurfIds=new HashMap<String,String>();
					HashMap<String,ArrayList<String>> deepIdToNumberedBubble=new HashMap<String,ArrayList<String>>();
					HashMap<String,String> surfIdToDeepIds=new HashMap<String,String>();
					HashMap<String,ArrayList<String>> deepToSurfNodes=new HashMap<String,ArrayList<String>>();
					
					HashMap<String,String> deepIdToBubbleGovernor=new HashMap<String,String>();
					
					HashMap<String,String> surfIdToDeprel=new HashMap<String,String>();
					HashMap<String,String> surfIdToHead=new HashMap<String,String>();*/
					
					//addLine(String targeAttachments, String bubbleGovernor, ArrayList<String> lexFeatures, ArrayList<String> bubbleArray, String deepId, String deepChild, CoNLLHash deepSentence, boolean train, String bubble) {
					
					
					itDeepIds=deepIds.iterator();
					while(itDeepIds.hasNext()){
						String id=itDeepIds.next();
						
						
						
						String bubble=idDeepToBubble.get(id);
						String bubbleGovernor=deepIdToBubbleGovernor.get(id);
						
						//**********************************************************************
						String featsSurface="";
						String featsVerbs="";
						String featsDeep=deepSentence.getFEAT(id);
						if (featsDeep.contains("number")){
							String number=CoNLLHash.getSubFeat(deepSentence.getFEAT(id),"number");
							if (!number.equals("")) featsSurface+="number="+number+"|";
						}
						if (featsDeep.contains("gender")){
							String gender=CoNLLHash.getSubFeat(deepSentence.getFEAT(id),"gender");
							if (!gender.equals("")) featsSurface+="gender="+gender+"|";
						}
						if (featsDeep.contains("tense")){
							String tense=CoNLLHash.getSubFeat(deepSentence.getFEAT(id),"tense");
							if (!tense.equals("")) featsVerbs+="tense="+tense+"|";
						}
						if (featsDeep.contains("finiteness")){
							String finiteness=CoNLLHash.getSubFeat(deepSentence.getFEAT(id),"finiteness");
							if (!finiteness.equals("")) featsVerbs+="finiteness="+finiteness+"|";
						}
						if (featsSurface.endsWith("|")){
							featsSurface=featsSurface.substring(0, featsSurface.length()-1);
						}
						if (featsVerbs.endsWith("|")){
							featsVerbs=featsVerbs.substring(0, featsVerbs.length()-1);
						}
						//**********************************************************************
						
						
						ArrayList<String> numberedBubble=deepIdToNumberedBubble.get(id);
						
						ArrayList<String> lexFeatures=new ArrayList<String>();
						//----
						//System.out.println(bubble);
						ArrayList<String> surfNodes=deepToSurfNodes.get(id);
						
						String idBubbleGovernor="-1";
						if (!numberedBubble.isEmpty() && !numberedBubble.toString().equals("[]")){
							//System.out.println(numberedBubble);
							try{
								idBubbleGovernor=surfNodes.get(numberedBubble.indexOf(bubbleGovernor));
							}catch(Exception e){
								idBubbleGovernor="0";
							}
						}
						
						//System.out.println(surfNodesArrayList);
						if (surfNodes!=null){
						Iterator<String> itS=surfNodes.iterator();
							while(itS.hasNext()){
								String s=itS.next();
								lexFeatures.add(surfIdToWords.get(s));
								
								//**********************************************************************************
								if (!surfIdToPos.get(s).equals("IN") && ! surfIdToPos.get(s).equals("RB")) {
									if (id.equals(idBubbleGovernor) && surfIdToPos.get(s).startsWith("V")){
										surfIdToFeats.put(s, featsSurface+"|"+featsVerbs);
									}
									else {
										surfIdToFeats.put(s,featsSurface);
									}
								}
								else {
									surfIdToFeats.put(s,"_");
								}
								//**********************************************************************************
								
							}
						}
						//System.out.println(lexFeatures);
						
						
						
						ArrayList<String> deepChilds=deepSentence.getChilds(id);
						Iterator<String> childIt=deepChilds.iterator();
						while(childIt.hasNext()){
							String deepChild=childIt.next();
							mbea.generateTestingFile(bubble);
							//public void addLine(String targiAttachments, ArrayList<String> lexFeatures, ArrayList<String> bubbleArray, String deepId, CoNLLHash deepSentence, boolean train, String bubble) {
							mbea.addLine("?", bubbleGovernor, lexFeatures, numberedBubble, id, deepChild, deepSentence, false, bubble);
							mbea.closeBuffers(false);
							
							HashMap<String,Integer> mapping=mbea.getMappingClassifiers();
	
							String[] args4 =new String[4];
							String linksString="";
							//System.out.println(bubble);
							if (!bubble.equals("[]") && mapping.get(bubble)!=null){
								args4[1]="eAttachment_svm_test_"+mapping.get(bubble)+".svm";
								args4[2]="eAttachment_svm_"+mapping.get(bubble)+".svm.model";
								args4[3]="eAttachment_output_"+mapping.get(bubble)+".svm";
								args4[0]="-q"; //quiet mode
								try {
									svm_predict.main(args4);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								linksString=mbea.getSVMeAttachmentsSingleFile("eAttachment_output_"+mapping.get(bubble)+".svm");
							}
							else if (mapping.get(bubble)==null){
								StringTokenizer stBub=new StringTokenizer(bubble);
								ArrayList<String> nodes=new ArrayList<String>();
								String deep="";
								while(stBub.hasMoreTokens()){
									String s=stBub.nextToken(",");
									s=s.replaceAll(" ", "").replaceAll("\\[","").replaceAll("\\]","");
									nodes.add(s);
									if (s.contains("deep")) deep=s;
								}
								linksString=deep+":DEP";
							}
								//System.out.println(linksString);
								String childBubbleGovernor=deepIdToBubbleGovernor.get(deepChild);
								ArrayList<String> numberedBubbleChild=deepIdToNumberedBubble.get(deepChild);
								//System.out.println(numberedBubbleChild);
								//System.out.println(surfNodes);
								ArrayList<String> surfNodesChild=deepToSurfNodes.get(deepChild);
								//System.out.println(surfNodesChild);
								if (!surfNodesChild.isEmpty()) {
									if (surfNodesChild.size()>numberedBubbleChild.indexOf(childBubbleGovernor)){
									String idChildBubbleGovernor=surfNodesChild.get(numberedBubbleChild.indexOf(childBubbleGovernor));
									//System.out.println(numberedBubble);
									//System.out.println(linksString);
									if (linksString.startsWith(":")){//???????????????????????????????????????????????????
										surfIdToDeprel.put(idChildBubbleGovernor, linksString.replaceAll(":",""));
										surfIdToHead.put(idChildBubbleGovernor, surfNodes.get(0));
									}
									else {
										try{
										surfIdToDeprel.put(idChildBubbleGovernor, linksString.split(":")[1]);
										surfIdToHead.put(idChildBubbleGovernor, surfNodes.get(numberedBubble.indexOf(linksString.split(":")[0])));
										}catch(Exception e){
											
										}
									}
								}
								}
							//this is the link! just make the connection!! :)
							//}
							/*else {
								//default attachments!!
							}*/
						}
						
					}
					//*****************************************************************************************************************************************************										
								
					
					
					
					//---------------------------------------------------------------------
					
					//OUTPUT GENERATION.
					
					////HashMap<String,String> surfIdToDeprel=new HashMap<String,String>();
					//HashMap<String,String> surfIdToHead=new HashMap<String,String>();
					
					Set<String> idsSurf=surfIdToWords.keySet();
					
					for (int i=1;i<idsSurf.size()+1;i++){
						String idS=i+"";
						if (idsSurf.contains(idS)) {
							String feat=surfIdToFeats.get(idS);
							String head=surfIdToHead.get(idS);
							String deprel=surfIdToDeprel.get(idS); //SO FAR UNLABELLED... if necessary, we can make another classifier, but I don't see it right now...
							
							if (head==null && deprel==null) {
								head="0";
								deprel="ROOT";
							}
							if (deprel.equals("analyt_pass") || deprel.equals("analyt_perf")){
								feat+="|finiteness=PART";
							}
							if (deprel.equals("analyt_progr")){
								feat+="|finiteness=GER";
							}
							if (feat==null || feat.equals("")) feat="_";
							//System.out.println(feat);
							//feat="_";
							if (feat.startsWith("|")){
								feat=feat.substring(1,feat.length());
							}
							String conllLine=idS+"\t"+surfIdToWords.get(idS)+"\t"+surfIdToWords.get(idS)+"\t"+surfIdToWords.get(idS)+"\t"+surfIdToPos.get(idS)+"\t"+surfIdToPos.get(idS)+"\t"+feat+"\t"+feat+"\t"+head+"\t"+head+"\t"+deprel+"\t"+deprel+"\t"+"_"+"\t"+"_"+"\n";
							bw.write(conllLine);
							
//							String conll10Line=idS+"\t"+surfIdToWords.get(idS)+"\t"+surfIdToWords.get(idS)+"\t"+surfIdToPos.get(idS)+"\t"+surfIdToPos.get(idS)+"\t"+"\t"+feat+"\t"+"_"+"\t"+head+"\t"+"_"+"\t"+deprel+"\t"+deprel+"\t"+"_"+"\t"+"_"+"\n";
						}
					}
					
					bw.write("\n");
				
				
				
			
				//formDeep+="("+sentenceCounter+")";
				/*ArrayList<String> bubble=new ArrayList<String>();
				
				
				Iterator<String> itLemma=idToLemma.keySet().iterator();
				while(itLemma.hasNext()){
					String idKey=itLemma.next();
					mld.addLine(idToLemma.get(idKey), bubble.toString(), bubble, id, deepSentence, false, idToPos.get(idKey));
				}
				
				// Generate LIBSVM MODEL WITH THE BUBBLE.
				//bubbleString=bubbleString.toString().replaceAll("]","");
				//Collections.sort(bubble);
				
				
				
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
		//System.out.println(hsBubbles.keySet()); //it should output the spos nodes.
		//System.out.println(posToWord.keySet());
		//System.out.println(hsBubbles.get("N"));
		//System.out.println(posToWord.get("IN(deep)"));
		*/
		}
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		/*
		mbd.closeBuffers(false);
		mld.closeBuffers(false);
		//mbd.closeBuffer(false);
		//System.out.println(posToWord);
		*/
		
	}
	
	
	


public void generateBubblesAndLemmasForTraining(){
	
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
			
			String posDeep=CoNLLHash.getSubFeat(deepSentence.getFEAT(id),"spos");
			if (posDeep.equals("")) posDeep="_";
			//formDeep+="("+sentenceCounter+")";
			ArrayList<String> bubble=new ArrayList<String>();
			
			boolean idxFound=true;
			int cont=0;
			
			ArrayList<String> surfNodes=new ArrayList<String>();
			HashMap<String,String> idToLemma=new HashMap<String,String>();
			HashMap<String,String> idToPos=new HashMap<String,String>();
			HashMap<String,String> idToDeprel=new HashMap<String,String>();
			HashMap<String,String> idToHead=new HashMap<String,String>();
			
			String deepNode="";
			
			while(idxFound){
				String idx="id"+cont;
				String feats=deepSentence.getFEAT(id);
				String surfNode=CoNLLHash.getSubFeat(feats, idx);
				if (!surfNodes.contains(surfNode)){
					
					//System.out.println(idx);
					if (!surfNode.equals("")) {
						//System.out.println(surfNode);
						if (!surfNode.contains("_") && !surfNode.contains("bis") && !surfNode.equals("")) {
							//String pos=surfaceSentence.getPOS(surfNode);
							String pos=surfaceSentence.getPOS(surfNode);
							surfNodes.add(surfNode);
							
							
							if (cont==0){
								pos+="(deep)";
								deepNode=surfNode;
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
									idToDeprel.put(surfNode, surfaceSentence.getDeprel(surfNode).toLowerCase());
									idToHead.put(surfNode, surfaceSentence.getHead(surfNode).toLowerCase());
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
			if (!bubbles2More.contains(bubble.toString()) && bubble.size()>1) {
				bubbles2More.add(bubble.toString());
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
				
				
				
			
				
			//----------------------------------------------------------------------------------------------
				
				//Generate internal attachments
				String bubbleGovernor="";
				ArrayList<String> numberedBubble=new ArrayList<String>();
				ArrayList<String> lexFeatures=new ArrayList<String>();
				if (bubble.size()>1) {
					
					
				//	System.out.println(surfNodes);
					int k=0;
					ArrayList<String> links=new ArrayList<String>();
					//System.out.println(deepSentence.getLemma(id));
					int counter=0;
					HashMap<String,String> correspondences=new HashMap<String,String>();
					HashMap<String,Integer> contPos=new HashMap<String,Integer>();
					Iterator<String> surfIt=surfNodes.iterator();
					while(surfIt.hasNext()){
						String surf=surfIt.next();
						String pos=surfaceSentence.getPOS(surf);
						if (contPos.containsKey(pos)){
							if (!surf.equals(deepNode)){
								Integer n=contPos.get(pos);
								n++;
								contPos.put(pos, n);
								correspondences.put(surf, pos+"_"+contPos.get(pos));
							}
						}
						else {
							if (!surf.equals(deepNode)){
								contPos.put(pos, 0);
								correspondences.put(surf, pos+"_"+contPos.get(pos));
							}
						}
					}
					//System.out.println(correspondences);
					
					
					surfIt=surfNodes.iterator();
					while(surfIt.hasNext()){
						String sNode=surfIt.next();
						//String pos=surfaceSentence.getPOS(sNode);
						String pos;
						if (sNode.equals(deepNode)){
							pos=surfaceSentence.getPOS(sNode);
						}
						else {
							pos=correspondences.get(sNode);
						}
						String head=surfaceSentence.getHead(sNode);
						String posHead=correspondences.get(head);
						String deprel=surfaceSentence.getDeprel(sNode);
						String lemma=surfaceSentence.getLemma(sNode);
						lexFeatures.add(lemma);
						if (surfNodes.contains(head) && head.equals(deepNode) && !pos.contains("__")){
							links.add(surfaceSentence.getPOS(head)+"(deep)->"+pos+":rel("+surfaceSentence.getDeprel(sNode)+")");
							counter++;
						}
						else if (surfNodes.contains(head) && sNode.equals(deepNode) && !pos.contains("__")) {
							links.add(surfaceSentence.getPOS(sNode)+"(deep)<-"+posHead+":rel("+surfaceSentence.getDeprel(sNode)+")");
							counter++;
						}
						else if (surfNodes.contains(head) && !pos.contains("__")) {
							if (Integer.parseInt(sNode)>Integer.parseInt(head)) links.add(posHead+"->"+pos+":rel("+surfaceSentence.getDeprel(sNode)+")");
							else links.add(pos+"<-"+posHead+":rel("+surfaceSentence.getDeprel(sNode)+")");
							counter++;
						}
					//	System.out.print(surfaceSentence.getLemma(sNode)+"--->");
					//	System.out.println(bubble.get(k));
						k++;
					}
					
					//System.out.println(bubble.toString()+"::::: "+links);
					mbia.addLine(links.toString(), lexFeatures, bubble, id, deepSentence, true, bubble.toString());
					//bubbleToInternalLinks.put(bubble, 
				
				
				
				//-----------------------------------------------------------------------------------------------------------
				//given the links, get the governor
				
				String linksString=links.toString();
				linksString=linksString.replaceAll("\\[","");
				linksString=linksString.replaceAll("\\]","");
				//System.out.println(linksString);
				StringTokenizer st=new StringTokenizer(linksString,",");
				
				numberedBubble=new ArrayList<String>();
				
				while(st.hasMoreTokens()){
					String linkS=st.nextToken();
					String[] spLinks=null;
					if (linkS.contains("->")){
						spLinks=linkS.split("->");
					}
					else if (linkS.contains("<-")){
						spLinks=linkS.split("<-");
					}
					String splinks0=spLinks[0].replaceAll(" ", "");
					String splinks1=spLinks[1].replaceAll(" ", "");
					if (!numberedBubble.contains(splinks0)) numberedBubble.add(spLinks[0].replaceAll(" ",""));
					if (!numberedBubble.contains(splinks1.split(":")[0]))numberedBubble.add(splinks1.split(":")[0].replaceAll(" ",""));
					
				}
				
				//System.out.println("NumberedBubble:"+numberedBubble);
				
				st=new StringTokenizer(linksString,",");
				
				ArrayList<String> internals=new ArrayList<String>();
				while(st.hasMoreTokens()){
					String link=st.nextToken(",");
					//System.out.println(link);
					if (link.contains("->")){
						String[] spLinks=link.split("->");
						internals.add(spLinks[1].split(":")[0].replaceAll(" ", ""));
					}
					else if (link.contains("<-")){
						String[] spLinks=link.split("<-");
						internals.add(spLinks[0].split(":")[0].replaceAll(" ", ""));
					}
				}
				//System.out.println("Internals:"+internals);
				String bubbleToProcs=numberedBubble.toString();
				bubbleToProcs=bubbleToProcs.replaceAll("\\[","");
				bubbleToProcs=bubbleToProcs.replaceAll("\\]","");
				bubbleToProcs=bubbleToProcs.replaceAll(" ","");
				
				StringTokenizer st2=new StringTokenizer(bubbleToProcs,",");
				//System.out.println(bubbleToProcs);
				int l=0;
				while(st2.hasMoreTokens()){
					String posS=st2.nextToken(",");
					
					if (!internals.contains(posS)){
						bubbleGovernor=posS;
					}
					
				}
				//System.out.println("Governor:"+bubbleGovernor);
				
			} //bubble.size()>1
			else if (!bubble.isEmpty()){ //very easy in the case of a single node
				bubbleGovernor=bubble.get(0);
				numberedBubble.addAll(bubble);
			}
				
			if (numberedBubble.isEmpty() && !bubble.isEmpty()){
				bubbleGovernor=bubble.get(0);
				numberedBubble.addAll(bubble);
			}
				
			//-----------------------------------------------------------------------------------------------------------
			if (!bubble.isEmpty()) { //every deep node corresponds to a bubble, has an identifier which is the deep identifier
				//System.out.println("**********");
				//System.out.println("bubble:"+bubble);
				
				ArrayList<String> deepChilds=deepSentence.getChilds(id); 
				Iterator<String> chIt=deepChilds.iterator();
				//System.out.println(lexFeatures);
				
				//System.out.println(surfNodes);
				//System.out.println(numberedBubble);
				//System.out.println("Bubblegov:"+bubbleGovernor);
				String idSurfGovernor=surfNodes.get(numberedBubble.indexOf(bubbleGovernor));
				//System.out.println("Idgov:"+idSurfGovernor);
				
				//System.out.println("************");
				
				while(chIt.hasNext()){
					String deepChild=chIt.next(); //this is the deep child.
					//System.out.println("DEEPCHILD:"+deepChild);
					//find the head inside the current bubble. This is the training process, so let's check the Surface treebank.
					String deepDeprel=deepSentence.getDeprel(deepChild);
					String childFeats=deepSentence.getFEAT(deepChild);
					
					//find governor of this deepchild.
					//String feats
					ArrayList<String> surfNodesChild=new ArrayList<String>();
					String idx="id";
					int x=0;
					
					String surfaceChild="";
					String surfaceDeprel="";
					
					String posCase="";
					boolean found=false;
					String idChildSurface="";
					while(x<10){
						if (childFeats.contains(idx+x+"=")){
							idChildSurface=deepSentence.getSubFeat(childFeats, idx+x);
							
							//System.out.println(idChildSurface);
							if (!idChildSurface.contains("_")&& !idChildSurface.contains("prosubj")&& !idChildSurface.contains("bis")){
									String headSurface=surfaceSentence.getHead(idChildSurface);
									String deprelSurface=surfaceSentence.getDeprel(idChildSurface);
							
									//System.out.println(headSurface+"?in"+surfNodes);
									//System.out.println(deprelSurface);
									if (surfNodes.contains(headSurface)){ // the head surface should go to any of the surface nodes of the bubble
										found=true;
										surfaceChild=idChildSurface;
										surfaceDeprel=deprelSurface;
										for(int i=0;i<numberedBubble.size();i++){
											if (surfNodes.get(i).equals(headSurface)){
												posCase=numberedBubble.get(i);
											}
										}
									}
							}
						
						}
						
						x++;
					}
					/*if (!found && !idChildSurface.contains("_")&& !idChildSurface.contains("prosubj")&& !idChildSurface.contains("bis")){
						System.out.println("ERROR GRAVE");
						System.out.println(idChildSurface);
					}*/
					if (found){
						
						String caso=posCase+":"+surfaceDeprel;					
						//String caso=surfaceDeprel;
						//String caso=posCase;
						//String posChild=deepSentence.getPOS(deepChild);
						//meter caso en el SVM!! :)
						//generar el caso.
						mbea.addLine(caso, bubbleGovernor, lexFeatures, numberedBubble, id, deepChild, deepSentence, true, bubble.toString());
					}
				}
			}
			
		}
		
		sentenceCounter++;
	}
	//System.out.println(hsBubbles.keySet()); //it should output the spos nodes.
	//System.out.println(posToWord.keySet());
	//System.out.println(hsBubbles.get("N"));
	//System.out.println(posToWord.get("IN(deep)"));
	
	mbd.closeBuffers(true);
	mld.closeBuffers(true);
	mbia.closeBuffers(true);
	mbea.closeBuffers(true);
	
	
	
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
			
				String posDeep=CoNLLHash.getSubFeat(deepSentence.getFEAT(id),"spos");
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
	            
	           	GeneratorFromDeepToSurfaceWF transducer=new GeneratorFromDeepToSurfaceWF(line.getOptionValue("s"),line.getOptionValue("d"), line.getOptionValue("dt"));
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
