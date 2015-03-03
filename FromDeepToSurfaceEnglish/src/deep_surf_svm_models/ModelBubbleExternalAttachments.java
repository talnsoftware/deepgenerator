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
public class ModelBubbleExternalAttachments {
	
	int max=0;
	
	int next=0;
	int nexteAttachments=0;
	
	private HashMap<String, String> featureTranslation=new HashMap<String,String>();
	private HashMap<String, String> eAttachmentTranslation=new HashMap<String,String>();
	private HashMap<String,BufferedWriter> trainWriters;
	private HashMap<String,BufferedWriter> testWriters;
	ArrayList<String> classifiers=new ArrayList<String>();
	HashMap<String,Integer> mappingClassifiers=new HashMap<String,Integer>();

	
	public HashMap<String, Integer> getMappingClassifiers() {
		return mappingClassifiers;
	}


	public ArrayList<String> getClassifiers() {
		return classifiers;
	}


	public ModelBubbleExternalAttachments() {
		trainWriters=new HashMap<String,BufferedWriter>();
		testWriters=new HashMap<String,BufferedWriter>();
		
	}
	
	public void generateTestingFile (String bubbleF) {
		BufferedWriter bw2;
		try {
			bw2 = new BufferedWriter(new FileWriter("eAttachment_svm_test_"+mappingClassifiers.get(bubbleF)+".svm"));
			testWriters.remove(bubbleF);
			testWriters.put(bubbleF,bw2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String getSVMeAttachments(String svmeAttachments) {
		//it is the other way around.. we should find the eAttachments that corresponds to the svm label that maps eAttachmentss (svmeAttachments). Hashmap process backwards.
		Set<String> eAttachmentss=eAttachmentTranslation.keySet();
		Iterator<String> dpIt=eAttachmentss.iterator();
		while (dpIt.hasNext()) {
			String eAttachments=dpIt.next();
			String svmAux=eAttachmentTranslation.get(eAttachments);
			if (svmAux.equals(svmeAttachments)) return eAttachments; 
		}
		return null;
	}
	
	public String getSVMeAttachmentsSingleFile(String file) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String svmBubble="";
			while(br.ready()){
				svmBubble=br.readLine();
			}
			br.close();
			Double eAttachments=Double.parseDouble(svmBubble);
			Integer eAttachmentsInt=eAttachments.intValue();
			Set<String> eAttachmentss=eAttachmentTranslation.keySet();
			Iterator<String> dpIt=eAttachmentss.iterator();
			while (dpIt.hasNext()) {
				String eAttachmentsS=dpIt.next();
				String svmAux=eAttachmentTranslation.get(eAttachmentsS);
				if (svmAux.equals(eAttachmentsInt.toString())) return eAttachmentsS; 
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
	
	private void addNeweAttachments (String eAttachments){
		
		String svmFeat=eAttachmentTranslation.get(eAttachments);
		if (svmFeat==null) {
			nexteAttachments++;
			eAttachmentTranslation.put(eAttachments,""+nexteAttachments);
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
	 * @param targeAttachments
	 * @param surfaceSentence 
	 * @param surfaceId 
	 * @param hypernode
	 * @param train
	 */
	public void addLine(String targeAttachments, String bubbleGovernor, ArrayList<String> lexFeatures, ArrayList<String> bubbleArray, String deepId, String deepChild, CoNLLHash deepSentence, boolean train, String bubble) {
		//SURFACE POS COMES FROM THE  CLASSIFIER THAT GENERATES THE BUBBLE
		if (train && trainWriters.get(bubble)==null){
			classifiers.add(bubble);
			mappingClassifiers.put(bubble,max);
			max++;
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("eAttachment_svm_"+mappingClassifiers.get(bubble)+".svm"));
				trainWriters.put(bubble,bwTrain);
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("eAttachment_svm_test_"+mappingClassifiers.get(bubble)+".svm"));
				testWriters.put(bubble,bwTest);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		//System.out.println(eAttachments);
		String line="";
			boolean write=false;
			//if (true) {
			if (train) {
				addNeweAttachments(targeAttachments);
				line+=eAttachmentTranslation.get(targeAttachments);
			}
			else {
				line+="1";
			}
			
			
			this.addNewFeature("bg="+bubbleGovernor);
			line+=" "+featureTranslation.get("bg="+bubbleGovernor);
			
			Iterator<String> lexIt=lexFeatures.iterator();
			int k=0;
			/*while(lexIt.hasNext()){
				String lex=lexIt.next();
				this.addNewFeature("lex="+lex+k);
				line+=" "+featureTranslation.get("lex="+lex+k);
				
			}*/
			
			Iterator<String> posIt=bubbleArray.iterator();
			k=0;
			while(posIt.hasNext()){
				String pos=posIt.next();
				this.addNewFeature("pos="+pos+k);
				line+=" "+featureTranslation.get("pos="+pos+k);
				
			}
			
			this.addNewFeature("posChild="+deepSentence.getPOS(deepChild));
			line+=" "+featureTranslation.get("posChild="+deepSentence.getPOS(deepChild));
			
			
			
			
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
			
			//eAttachments
			/*this.addNewFeature("eAttachments="+eAttachments);
			line+=" "+featureTranslation.get("eAttachments="+eAttachments);*/
			
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
			
			/*while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("spos")) {
					this.addNewFeature("pos+s="+s+deepSentence.getPOS(deepId));
					line+=" "+featureTranslation.get("pos+s="+s+deepSentence.getPOS(deepId));
				}
			}*/
			
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
			st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("voice")) {
					this.addNewFeature("voice="+s);
					line+=" "+featureTranslation.get("voice="+s);
				}
			}
			
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
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("definiteness")) {
					this.addNewFeature("def="+s);
					line+=" "+featureTranslation.get("def="+s);
				}
			}*/
			
			//finiteness
			st=new StringTokenizer(deepSentence.getFEAT(deepId));
				while(st.hasMoreTokens()) {
					String s=st.nextToken("|");
					if (s.contains("finiteness")) {
						this.addNewFeature("fin="+s);
						line+=" "+featureTranslation.get("fin="+s);
					}
				}
				
				//finiteness child
				st=new StringTokenizer(deepSentence.getFEAT(deepChild));
					while(st.hasMoreTokens()) {
						String s=st.nextToken("|");
						if (s.contains("finiteness")) {
							this.addNewFeature("finChild="+s);
							line+=" "+featureTranslation.get("finChild="+s);
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
			
			this.addNewFeature("depChild="+deepSentence.getDeprel(deepChild));
			line+=" "+featureTranslation.get("depChild="+deepSentence.getDeprel(deepChild));
			
			
			//eAttachments
			/*this.addNewFeature("eAttachments="+deepSentence.geteAttachments(deepId));
			line+=" "+featureTranslation.get("eAttachments="+deepSentence.geteAttachments(deepId));*/
			
			
			String headId=deepSentence.getHead(deepId);
			if (headId!=null){
				if (headId.equals("0")){
					this.addNewFeature("eAttachmentsH=root");
					line+=" "+featureTranslation.get("eAttachmentsH=root");
				}
				else {
					//SIBLINGS
					ArrayList<String> siblings=deepSentence.getSiblings(headId);
					Iterator<String> itSib=siblings.iterator();
					while(itSib.hasNext()) {
						String sib=itSib.next();
						if (!sib.equals(deepId)) {
							String sibDeprel=deepSentence.getDeprel(sib);
							
							this.addNewFeature("sibDeprel="+sibDeprel);
							line+=" "+featureTranslation.get("sibDeprel="+sibDeprel);
														
							String sibPos=deepSentence.getPOS(sib);						
							this.addNewFeature("sibPos="+sibPos);
							line+=" "+featureTranslation.get("sibPos="+sibPos);
							
							String sibeAttachments=deepSentence.getForm(sib);						
							this.addNewFeature("sibeAttachments="+sibeAttachments);
							line+=" "+featureTranslation.get("sibeAttachments="+sibeAttachments);

						}
					}
					
					
					
					StringTokenizer st2=new StringTokenizer(deepSentence.getFEAT(headId));
					
					//SPOS of head
					while(st2.hasMoreTokens()) {
						String s=st2.nextToken("|");
						if (s.contains("spos")) {
							//this.addNewFeature("sposH="+s);
							//line+=" "+featureTranslation.get("sposH="+s);
							
						}
					}
				}
			}
			
			try {
				
				if (train){	
					//System.out.println(surfacePOS);
					
					BufferedWriter bw=trainWriters.get(bubble);
					bw.write(line+"\n");
				}
				else {
					BufferedWriter bw2=testWriters.get(bubble);
					bw2.write(line+"\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
					
	}
	
	
	
	public void addLineForDeubbing(String targeAttachments, String bubbleGovernor, ArrayList<String> lexFeatures, ArrayList<String> bubbleArray, String deepId, String deepChild, CoNLLHash deepSentence, boolean train, String bubble) {
		//SURFACE POS COMES FROM THE  CLASSIFIER THAT GENERATES THE BUBBLE
		if (train && trainWriters.get(bubble)==null){
			classifiers.add(bubble);
			mappingClassifiers.put(bubble,max);
			max++;
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("eAttachment_svm_"+mappingClassifiers.get(bubble)+".svm"));
				trainWriters.put(bubble,bwTrain);
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("eAttachment_svm_test_"+mappingClassifiers.get(bubble)+".svm"));
				testWriters.put(bubble,bwTest);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		//System.out.println(eAttachments);
		String line="";
			boolean write=false;
			if (true) {
			//if (train) {
				addNeweAttachments(targeAttachments);
				line+=eAttachmentTranslation.get(targeAttachments);
			}
			else {
				line+="1";
			}
			
			
			this.addNewFeature("bg="+bubbleGovernor);
			line+=" "+featureTranslation.get("bg="+bubbleGovernor);
			
			Iterator<String> lexIt=lexFeatures.iterator();
			int k=0;
			/*while(lexIt.hasNext()){
				String lex=lexIt.next();
				this.addNewFeature("lex="+lex+k);
				line+=" "+featureTranslation.get("lex="+lex+k);
				
			}*/
			
			Iterator<String> posIt=bubbleArray.iterator();
			k=0;
			while(posIt.hasNext()){
				String pos=posIt.next();
				this.addNewFeature("pos="+pos+k);
				line+=" "+featureTranslation.get("pos="+pos+k);
				
			}
			
			this.addNewFeature("posChild="+deepSentence.getPOS(deepChild));
			line+=" "+featureTranslation.get("posChild="+deepSentence.getPOS(deepChild));
			
			
			
			
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
			
			//eAttachments
			/*this.addNewFeature("eAttachments="+eAttachments);
			line+=" "+featureTranslation.get("eAttachments="+eAttachments);*/
			
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
			
			/*while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("spos")) {
					this.addNewFeature("pos+s="+s+deepSentence.getPOS(deepId));
					line+=" "+featureTranslation.get("pos+s="+s+deepSentence.getPOS(deepId));
				}
			}*/
			
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
			st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("voice")) {
					this.addNewFeature("voice="+s);
					line+=" "+featureTranslation.get("voice="+s);
				}
			}
			
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
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("definiteness")) {
					this.addNewFeature("def="+s);
					line+=" "+featureTranslation.get("def="+s);
				}
			}*/
			
			//finiteness
			st=new StringTokenizer(deepSentence.getFEAT(deepId));
				while(st.hasMoreTokens()) {
					String s=st.nextToken("|");
					if (s.contains("finiteness")) {
						this.addNewFeature("fin="+s);
						line+=" "+featureTranslation.get("fin="+s);
					}
				}
				
				//finiteness child
				st=new StringTokenizer(deepSentence.getFEAT(deepChild));
					while(st.hasMoreTokens()) {
						String s=st.nextToken("|");
						if (s.contains("finiteness")) {
							this.addNewFeature("finChild="+s);
							line+=" "+featureTranslation.get("finChild="+s);
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
			
			this.addNewFeature("depChild="+deepSentence.getDeprel(deepChild));
			line+=" "+featureTranslation.get("depChild="+deepSentence.getDeprel(deepChild));
			
			
			//eAttachments
			/*this.addNewFeature("eAttachments="+deepSentence.geteAttachments(deepId));
			line+=" "+featureTranslation.get("eAttachments="+deepSentence.geteAttachments(deepId));*/
			
			
			String headId=deepSentence.getHead(deepId);
			if (headId!=null){
				if (headId.equals("0")){
					this.addNewFeature("eAttachmentsH=root");
					line+=" "+featureTranslation.get("eAttachmentsH=root");
				}
				else {
					//SIBLINGS
					ArrayList<String> siblings=deepSentence.getSiblings(headId);
					Iterator<String> itSib=siblings.iterator();
					while(itSib.hasNext()) {
						String sib=itSib.next();
						if (!sib.equals(deepId)) {
							String sibDeprel=deepSentence.getDeprel(sib);
							
							this.addNewFeature("sibDeprel="+sibDeprel);
							line+=" "+featureTranslation.get("sibDeprel="+sibDeprel);
														
							String sibPos=deepSentence.getPOS(sib);						
							this.addNewFeature("sibPos="+sibPos);
							line+=" "+featureTranslation.get("sibPos="+sibPos);
							
							String sibeAttachments=deepSentence.getForm(sib);						
							this.addNewFeature("sibeAttachments="+sibeAttachments);
							line+=" "+featureTranslation.get("sibeAttachments="+sibeAttachments);

						}
					}
					
					
					
					StringTokenizer st2=new StringTokenizer(deepSentence.getFEAT(headId));
					
					//SPOS of head
					while(st2.hasMoreTokens()) {
						String s=st2.nextToken("|");
						if (s.contains("spos")) {
							//this.addNewFeature("sposH="+s);
							//line+=" "+featureTranslation.get("sposH="+s);
							
						}
					}
				}
			}
			
			try {
				
				if (train){	
					//System.out.println(surfacePOS);
					
					BufferedWriter bw=trainWriters.get(bubble);
					bw.write(line+"\n");
				}
				else {
					BufferedWriter bw2=testWriters.get(bubble);
					bw2.write(line+"\n");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}	
					
	}

				
		
	
	private ArrayList<String> getSiblings(String head, CoNLLHash surfaceSentence) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString() {
		return this.featureTranslation.toString() + eAttachmentTranslation.toString();
	
	}

}
