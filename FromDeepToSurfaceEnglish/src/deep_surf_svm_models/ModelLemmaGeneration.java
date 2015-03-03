/**
 * 
 */
package deep_surf_svm_models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import deep_to_surf.CoNLLHash;


/**
 * * @author Miguel Ballesteros
 * Universitat Pompeu Fabra. 
 *
 */
public class ModelLemmaGeneration {
	

	
	int next=0;
	int nextLemma=0;
	
	private HashMap<String, String> featureTranslation=new HashMap<String,String>();
	private HashMap<String, String> lemmaTranslation=new HashMap<String,String>();
	private HashMap<String,BufferedWriter> trainWriters;
	private HashMap<String,BufferedWriter> testWriters;
	ArrayList<String> classifiers=new ArrayList<String>();

	
	public ArrayList<String> getClassifiers() {
		return classifiers;
	}


	public ModelLemmaGeneration() {
		trainWriters=new HashMap<String,BufferedWriter>();
		testWriters=new HashMap<String,BufferedWriter>();
		
	}
	
	public void generateTestingFile (String posSurf) {
		BufferedWriter bw2;
		try {
			bw2 = new BufferedWriter(new FileWriter("lemma_svm_test_"+posSurf+".svm"));
			testWriters.remove(posSurf);
			testWriters.put(posSurf,bw2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String getSVMLemma(String svmLemma) {
		//it is the other way around.. we should find the lemma that corresponds to the svm label that maps lemmas (svmlemma). Hashmap process backwards.
		Set<String> lemmas=lemmaTranslation.keySet();
		Iterator<String> dpIt=lemmas.iterator();
		while (dpIt.hasNext()) {
			String lemma=dpIt.next();
			String svmAux=lemmaTranslation.get(lemma);
			if (svmAux.equals(svmLemma)) return lemma; 
		}
		return null;
	}
	
	public String getSVMLemmaSingleFile(String file) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String svmBubble="";
			while(br.ready()){
				svmBubble=br.readLine();
			}
			br.close();
			Double lemma=Double.parseDouble(svmBubble);
			Integer lemmaInt=lemma.intValue();
			Set<String> lemmas=lemmaTranslation.keySet();
			Iterator<String> dpIt=lemmas.iterator();
			while (dpIt.hasNext()) {
				String lemmaS=dpIt.next();
				String svmAux=lemmaTranslation.get(lemmaS);
				if (svmAux.equals(lemmaInt.toString())) return lemmaS; 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		
		return null;
	}
	
	
	
	public void closeBuffers(boolean train){
		try {
			if (train) {
				Iterator<String> it=trainWriters.keySet().iterator();
				while(it.hasNext()){
					BufferedWriter bw=trainWriters.get(it.next());
					bw.close();
				}
				
			}
			else {
				Iterator<String> it=testWriters.keySet().iterator();
				while(it.hasNext()){
					BufferedWriter bw=testWriters.get(it.next());
					bw.close();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void addNewFeature (String feature){
		
		String svmFeat=featureTranslation.get(feature);
		if (svmFeat==null) {
			next++;
			featureTranslation.put(feature,""+next+":1");
		}
		
	}
	
	private void addNewLemma (String lemma){
		
		String svmFeat=lemmaTranslation.get(lemma);
		if (svmFeat==null) {
			nextLemma++;
			lemmaTranslation.put(lemma,""+nextLemma);
			//System.out.println(deprelTranslation);
		}
		
	}
	
	/**
	 * 
	 * 
	 * @param form
	 * @param bubble
	 * @param pos
	 * @param feats
	 * @param deprel
	 * @param targLemma
	 * @param surfaceSentence 
	 * @param surfaceId 
	 * @param hypernode
	 * @param train
	 */
	public void addLine(String targLemma, String bubble, ArrayList<String> bubbleArray, String deepId, CoNLLHash deepSentence, boolean train, String surfacePOS) {
		//SURFACE POS COMES FROM THE  CLASSIFIER THAT GENERATES THE BUBBLE
		if (train && trainWriters.get(surfacePOS)==null){
			classifiers.add(surfacePOS);
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("lemma_svm_"+surfacePOS+".svm"));
				trainWriters.put(surfacePOS,bwTrain);
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("lemma_svm_test_"+surfacePOS+".svm"));
				testWriters.put(surfacePOS,bwTest);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		//System.out.println(lemma);
		String line="";
			boolean write=false;
			//if (true) {
			if (train) {
				addNewLemma(targLemma);
				line+=lemmaTranslation.get(targLemma);
			}
			else {
				line+="1";
			}
			StringTokenizer st=new StringTokenizer(deepSentence.getFEAT(deepId));
			
			//POS
			/*this.addNewFeature("pos="+deepSentence.getPOS(deepId));
			line+=" "+featureTranslation.get("pos="+deepSentence.getPOS(deepId));*/
			
			//SPOS
			/*while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("spos")) {
					this.addNewFeature("spos="+s);
					line+=" "+featureTranslation.get("spos="+s);
				}
			}*/
			
			//lemma
			/*this.addNewFeature("lemma="+lemma);
			line+=" "+featureTranslation.get("lemma="+lemma);*/
			
			//bubble
			/*Iterator<String> it=bubbleArray.iterator();
			int cont=0;
			while(it.hasNext()){
				String s=it.next();
				this.addNewFeature("bubble"+cont+"="+s);
				line+=" "+featureTranslation.get("bubble"+cont+"="+s);
				cont++;
			}*/
		
			
			/*this.addNewFeature("bubble="+bubble);
			line+=" "+featureTranslation.get("bubble="+bubble);*/
			
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("spos")) {
					this.addNewFeature("pos+s="+s+deepSentence.getPOS(deepId));
					line+=" "+featureTranslation.get("pos+s="+s+deepSentence.getPOS(deepId));
				}
			}
			
			//tem_constituency
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("tem_constituency")) {
					this.addNewFeature("tc="+s);
					line+=" "+featureTranslation.get("tc="+s);
				}
			}*/
			
			//voice
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("voice")) {
					this.addNewFeature("voice="+s);
					line+=" "+featureTranslation.get("voice="+s);
				}
			}*/
			
			//tense
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("tense")) {
					this.addNewFeature("tense="+s);
					line+=" "+featureTranslation.get("tense="+s);
				}
			}*/
			
			//definiteness
			if (surfacePOS.equals("DT")){
				st=new StringTokenizer(deepSentence.getFEAT(deepId));
				while(st.hasMoreTokens()) {
					String s=st.nextToken("|");
					if (s.contains("definiteness")) {
						this.addNewFeature("def="+s);
						line+=" "+featureTranslation.get("def="+s);
					}
				}
			}
			
			//finiteness
			if (surfacePOS.equals("PP")){
				st=new StringTokenizer(deepSentence.getFEAT(deepId));
				while(st.hasMoreTokens()) {
					String s=st.nextToken("|");
					if (s.contains("finiteness")) {
						this.addNewFeature("fin="+s);
						line+=" "+featureTranslation.get("fin="+s);
					}
				}
			}
			//sent_type
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("sent_type")) {
					this.addNewFeature("sent_type="+s);
					line+=" "+featureTranslation.get("sent_type="+s);
				}
			}*/
			
			//number
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("number")) {
					this.addNewFeature("number="+s);
					line+=" "+featureTranslation.get("number="+s);
				}
			}*/
			
			//mood
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("mood")) {
					this.addNewFeature("mood="+s);
					line+=" "+featureTranslation.get("mood="+s);
				}
			}
			
			//gender
			st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("gender")) {
					this.addNewFeature("gender="+s);
					line+=" "+featureTranslation.get("gender="+s);
				}
			}*/
			
			

			
			//DEPREL
			this.addNewFeature("dep="+deepSentence.getDeprel(deepId));
			line+=" "+featureTranslation.get("dep="+deepSentence.getDeprel(deepId));
			
			
			//LEMMA
			/*this.addNewFeature("lemma="+deepSentence.getForm(deepId));
			line+=" "+featureTranslation.get("lemma="+deepSentence.getForm(deepId));*/
			
			
			String headId=deepSentence.getHead(deepId);
			if (headId!=null){
				if (headId.equals("0")){
					this.addNewFeature("lemmaH=root");
					line+=" "+featureTranslation.get("lemmaH=root");
				}
				else {
					//SIBLINGS
					ArrayList<String> siblings=deepSentence.getSiblings(headId);
					Iterator<String> itSib=siblings.iterator();
					while(itSib.hasNext()) {
						String sib=itSib.next();
						if (!sib.equals(deepId)) {
							String sibDeprel=deepSentence.getDeprel(sib);
							
							//this.addNewFeature("sibDeprel="+sibDeprel);
							//line+=" "+featureTranslation.get("sibDeprel="+sibDeprel);
														
							/*String sibPos=deepSentence.getPOS(sib);						
							this.addNewFeature("sibPos="+sibPos);
							line+=" "+featureTranslation.get("sibPos="+sibPos);*/
							/*String sibLemma=deepSentence.getForm(sib);						
							this.addNewFeature("sibLemma="+sibLemma);
							line+=" "+featureTranslation.get("sibLemma="+sibLemma);*/

						}
					}
					
					
					//LEMMA of head
					this.addNewFeature("lemmaH="+deepSentence.getForm(headId));
					line+=" "+featureTranslation.get("lemmaH="+deepSentence.getForm(headId));
					
					
					StringTokenizer st2=new StringTokenizer(deepSentence.getFEAT(headId));
					
					//SPOS of head
					while(st2.hasMoreTokens()) {
						String s=st2.nextToken("|");
						if (s.contains("spos")) {
							this.addNewFeature("sposH="+s);
							line+=" "+featureTranslation.get("sposH="+s);
						}
					}
				}
			}
			
			try {
				
				if (train){	
					//System.out.println(surfacePOS);
					BufferedWriter bw=trainWriters.get(surfacePOS);
					bw.write(line+"\n");
				}
				else {
					BufferedWriter bw2=testWriters.get(surfacePOS);
					bw2.write(line+"\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
					
	}
	
	
	
	public void addLineForDeubbing(String targLemma, String bubble, ArrayList<String> bubbleArray, String deepId, CoNLLHash deepSentence, boolean train, String surfacePOS) {
		//SURFACE POS COMES FROM THE  CLASSIFIER THAT GENERATES THE BUBBLE
		if (train && trainWriters.get(surfacePOS)==null){
			classifiers.add(surfacePOS);
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("lemma_svm_"+surfacePOS+".svm"));
				trainWriters.put(surfacePOS,bwTrain);
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("lemma_svm_test_"+surfacePOS+".svm"));
				testWriters.put(surfacePOS,bwTest);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		//System.out.println(lemma);
		String line="";
			boolean write=false;
			if (true) {
			//if (train) {
				addNewLemma(targLemma);
				line+=lemmaTranslation.get(targLemma);
			}
			else {
				line+="1";
			}
			StringTokenizer st=new StringTokenizer(deepSentence.getFEAT(deepId));
			
			//POS
			/*this.addNewFeature("pos="+deepSentence.getPOS(deepId));
			line+=" "+featureTranslation.get("pos="+deepSentence.getPOS(deepId));*/
			
			//SPOS
			/*while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("spos")) {
					this.addNewFeature("spos="+s);
					line+=" "+featureTranslation.get("spos="+s);
				}
			}*/
			
			//lemma
			/*this.addNewFeature("lemma="+lemma);
			line+=" "+featureTranslation.get("lemma="+lemma);*/
			
			//bubble
			/*Iterator<String> it=bubbleArray.iterator();
			int cont=0;
			while(it.hasNext()){
				String s=it.next();
				this.addNewFeature("bubble"+cont+"="+s);
				line+=" "+featureTranslation.get("bubble"+cont+"="+s);
				cont++;
			}*/
		
			
			/*this.addNewFeature("bubble="+bubble);
			line+=" "+featureTranslation.get("bubble="+bubble);*/
			
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("spos")) {
					this.addNewFeature("pos+s="+s+deepSentence.getPOS(deepId));
					line+=" "+featureTranslation.get("pos+s="+s+deepSentence.getPOS(deepId));
				}
			}
			
			//tem_constituency
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("tem_constituency")) {
					this.addNewFeature("tc="+s);
					line+=" "+featureTranslation.get("tc="+s);
				}
			}*/
			
			//voice
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("voice")) {
					this.addNewFeature("voice="+s);
					line+=" "+featureTranslation.get("voice="+s);
				}
			}*/
			
			//tense
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("tense")) {
					this.addNewFeature("tense="+s);
					line+=" "+featureTranslation.get("tense="+s);
				}
			}*/
			
			//definiteness
			if (surfacePOS.equals("DT")){
				st=new StringTokenizer(deepSentence.getFEAT(deepId));
				while(st.hasMoreTokens()) {
					String s=st.nextToken("|");
					if (s.contains("definiteness")) {
						this.addNewFeature("def="+s);
						line+=" "+featureTranslation.get("def="+s);
					}
				}
			}
			
			//finiteness
			if (surfacePOS.equals("PP")){
				st=new StringTokenizer(deepSentence.getFEAT(deepId));
				while(st.hasMoreTokens()) {
					String s=st.nextToken("|");
					if (s.contains("finiteness")) {
						this.addNewFeature("fin="+s);
						line+=" "+featureTranslation.get("fin="+s);
					}
				}
			}
			//sent_type
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("sent_type")) {
					this.addNewFeature("sent_type="+s);
					line+=" "+featureTranslation.get("sent_type="+s);
				}
			}*/
			
			//number
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("number")) {
					this.addNewFeature("number="+s);
					line+=" "+featureTranslation.get("number="+s);
				}
			}*/
			
			//mood
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("mood")) {
					this.addNewFeature("mood="+s);
					line+=" "+featureTranslation.get("mood="+s);
				}
			}
			
			//gender
			st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("gender")) {
					this.addNewFeature("gender="+s);
					line+=" "+featureTranslation.get("gender="+s);
				}
			}*/
			
			

			
			//DEPREL
			this.addNewFeature("dep="+deepSentence.getDeprel(deepId));
			line+=" "+featureTranslation.get("dep="+deepSentence.getDeprel(deepId));
			
			
			//LEMMA
			/*this.addNewFeature("lemma="+deepSentence.getForm(deepId));
			line+=" "+featureTranslation.get("lemma="+deepSentence.getForm(deepId));*/
			
			
			String headId=deepSentence.getHead(deepId);
			if (headId!=null){
				if (headId.equals("0")){
					this.addNewFeature("lemmaH=root");
					line+=" "+featureTranslation.get("lemmaH=root");
				}
				else {
					//SIBLINGS
					ArrayList<String> siblings=deepSentence.getSiblings(headId);
					Iterator<String> itSib=siblings.iterator();
					while(itSib.hasNext()) {
						String sib=itSib.next();
						if (!sib.equals(deepId)) {
							String sibDeprel=deepSentence.getDeprel(sib);
							
							//this.addNewFeature("sibDeprel="+sibDeprel);
							//line+=" "+featureTranslation.get("sibDeprel="+sibDeprel);
														
							/*String sibPos=deepSentence.getPOS(sib);						
							this.addNewFeature("sibPos="+sibPos);
							line+=" "+featureTranslation.get("sibPos="+sibPos);*/
							/*String sibLemma=deepSentence.getForm(sib);						
							this.addNewFeature("sibLemma="+sibLemma);
							line+=" "+featureTranslation.get("sibLemma="+sibLemma);*/

						}
					}
					
					
					//LEMMA of head
					this.addNewFeature("lemmaH="+deepSentence.getForm(headId));
					line+=" "+featureTranslation.get("lemmaH="+deepSentence.getForm(headId));
					
					
					StringTokenizer st2=new StringTokenizer(deepSentence.getFEAT(headId));
					
					//SPOS of head
					while(st2.hasMoreTokens()) {
						String s=st2.nextToken("|");
						if (s.contains("spos")) {
							this.addNewFeature("sposH="+s);
							line+=" "+featureTranslation.get("sposH="+s);
						}
					}
				}
			}
			
			try {
				
				if (train){	
					//System.out.println(surfacePOS);
					BufferedWriter bw=trainWriters.get(surfacePOS);
					bw.write(line+"\n");
				}
				else {
					BufferedWriter bw2=testWriters.get(surfacePOS);
					bw2.write(line+"\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
					
	}

				
		
	
	private ArrayList<String> getSiblings(String head, CoNLLHash surfaceSentence) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString() {
		return this.featureTranslation.toString() + lemmaTranslation.toString();
	
	}

}
