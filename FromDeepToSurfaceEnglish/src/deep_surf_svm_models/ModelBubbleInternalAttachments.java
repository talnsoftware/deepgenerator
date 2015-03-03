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
public class ModelBubbleInternalAttachments {
	
	int max=0;
	
	int next=0;
	int nextiAttachments=0;
	
	private HashMap<String, String> featureTranslation=new HashMap<String,String>();
	public HashMap<String, String> iAttachmentTranslation=new HashMap<String,String>();
	private HashMap<String,BufferedWriter> trainWriters;
	private HashMap<String,BufferedWriter> testWriters;
	ArrayList<String> classifiers=new ArrayList<String>();
	public HashMap<String,Integer> mappingClassifiers=new HashMap<String,Integer>();

	
	public HashMap<String, Integer> getMappingClassifiers() {
		return mappingClassifiers;
	}


	public ArrayList<String> getClassifiers() {
		return classifiers;
	}


	public ModelBubbleInternalAttachments() {
		trainWriters=new HashMap<String,BufferedWriter>();
		testWriters=new HashMap<String,BufferedWriter>();
		
	}
	
	public void generateTestingFile (String bubbleF) {
		BufferedWriter bw2;
		try {
			bw2 = new BufferedWriter(new FileWriter("iattachment_svm_test_"+mappingClassifiers.get(bubbleF)+".svm"));
			testWriters.remove(bubbleF);
			testWriters.put(bubbleF,bw2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String getSVMiAttachments(String svmiAttachments) {

		
		//it is the other way around.. we should find the iAttachments that corresponds to the svm label that maps iAttachmentss (svmiAttachments). Hashmap process backwards.
		Set<String> iAttachmentss=iAttachmentTranslation.keySet();
		Iterator<String> dpIt=iAttachmentss.iterator();
		while (dpIt.hasNext()) {
			String iAttachments=dpIt.next();
			String svmAux=iAttachmentTranslation.get(iAttachments);
			if (svmAux.equals(svmiAttachments)) return iAttachments; 
		}
		return null;
	}
	
	public String getSVMiAttachmentsSingleFile(String file) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String svmBubble="";
			while(br.ready()){
				svmBubble=br.readLine();
			}
			br.close();
			Double iAttachments=Double.parseDouble(svmBubble);
			Integer iAttachmentsInt=iAttachments.intValue();
			Set<String> iAttachmentss=iAttachmentTranslation.keySet();
			Iterator<String> dpIt=iAttachmentss.iterator();
			while (dpIt.hasNext()) {
				String iAttachmentsS=dpIt.next();
				String svmAux=iAttachmentTranslation.get(iAttachmentsS);
				if (svmAux.equals(iAttachmentsInt.toString())) return iAttachmentsS; 
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
	
	private void addNewiAttachments (String iAttachments){
		
		String svmFeat=iAttachmentTranslation.get(iAttachments);
		if (svmFeat==null) {
			nextiAttachments++;
			iAttachmentTranslation.put(iAttachments,""+nextiAttachments);
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
	 * @param targiAttachments
	 * @param surfaceSentence 
	 * @param surfaceId 
	 * @param hypernode
	 * @param train
	 */
	public void addLine(String targiAttachments, ArrayList<String> lexFeatures, ArrayList<String> bubbleArray, String deepId, CoNLLHash deepSentence, boolean train, String bubble) {
		//SURFACE POS COMES FROM THE  CLASSIFIER THAT GENERATES THE BUBBLE
		if (train && trainWriters.get(bubble)==null){
			classifiers.add(bubble);
			mappingClassifiers.put(bubble,max);
			max++;
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("iattachment_svm_"+mappingClassifiers.get(bubble)+".svm"));
				trainWriters.put(bubble,bwTrain);
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("iattachment_svm_test_"+mappingClassifiers.get(bubble)+".svm"));
				testWriters.put(bubble,bwTest);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		//System.out.println(iAttachments);
			String line="";
			boolean write=false;
			if (train) {
				addNewiAttachments(targiAttachments);
				line+=iAttachmentTranslation.get(targiAttachments);
			}
			else {
				line+="1";
			}
			
			
			Iterator<String> lexIt=lexFeatures.iterator();
			int k=0;
			while(lexIt.hasNext()){
				String lex=lexIt.next();
				this.addNewFeature("lex="+lex+k);
				line+=" "+featureTranslation.get("lex="+lex+k);
				
			}
			
			Iterator<String> posIt=bubbleArray.iterator();
			k=0;
			while(posIt.hasNext()){
				String pos=posIt.next();
				this.addNewFeature("pos="+pos+k);
				line+=" "+featureTranslation.get("pos="+pos+k);
				
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
			
			//iAttachments
			/*this.addNewFeature("iAttachments="+iAttachments);
			line+=" "+featureTranslation.get("iAttachments="+iAttachments);*/
			
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
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
				while(st.hasMoreTokens()) {
					String s=st.nextToken("|");
					if (s.contains("finiteness")) {
						this.addNewFeature("fin="+s);
						line+=" "+featureTranslation.get("fin="+s);
					}
				}*/
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
			
			
			//iAttachments
			/*this.addNewFeature("iAttachments="+deepSentence.getiAttachments(deepId));
			line+=" "+featureTranslation.get("iAttachments="+deepSentence.getiAttachments(deepId));*/
			
			
			String headId=deepSentence.getHead(deepId);
			if (headId!=null){
				if (headId.equals("0")){
					this.addNewFeature("iAttachmentsH=root");
					line+=" "+featureTranslation.get("iAttachmentsH=root");
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
							/*String sibiAttachments=deepSentence.getiAttachments(sib);						
							this.addNewFeature("sibiAttachments="+sibiAttachments);
							line+=" "+featureTranslation.get("sibiAttachments="+sibiAttachments);*/

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
	
	
	
	public void addLineForDeubbing(String targiAttachments, ArrayList<String> lexFeatures, ArrayList<String> bubbleArray, String deepId, CoNLLHash deepSentence, boolean train, String bubble) {
		//SURFACE POS COMES FROM THE  CLASSIFIER THAT GENERATES THE BUBBLE
		if (train && trainWriters.get(bubble)==null){
			classifiers.add(bubble);
			mappingClassifiers.put(bubble,max);
			max++;
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("iattachment_svm_"+mappingClassifiers.get(bubble)+".svm"));
				trainWriters.put(bubble,bwTrain);
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("iattachment_svm_test_"+mappingClassifiers.get(bubble)+".svm"));
				testWriters.put(bubble,bwTest);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		//System.out.println(iAttachments);
		String line="";
			boolean write=false;
			if (true) {
			//if (train) {
				addNewiAttachments(targiAttachments);
				line+=iAttachmentTranslation.get(targiAttachments);
			}
			else {
				line+="1";
			}
			
			
			Iterator<String> lexIt=lexFeatures.iterator();
			int k=0;
			while(lexIt.hasNext()){
				String lex=lexIt.next();
				this.addNewFeature("lex="+lex+k);
				line+=" "+featureTranslation.get("lex="+lex+k);
				
			}
			
			Iterator<String> posIt=bubbleArray.iterator();
			k=0;
			while(posIt.hasNext()){
				String pos=posIt.next();
				this.addNewFeature("pos="+pos+k);
				line+=" "+featureTranslation.get("pos="+pos+k);
				
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
			
			//iAttachments
			/*this.addNewFeature("iAttachments="+iAttachments);
			line+=" "+featureTranslation.get("iAttachments="+iAttachments);*/
			
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
			/*st=new StringTokenizer(deepSentence.getFEAT(deepId));
				while(st.hasMoreTokens()) {
					String s=st.nextToken("|");
					if (s.contains("finiteness")) {
						this.addNewFeature("fin="+s);
						line+=" "+featureTranslation.get("fin="+s);
					}
				}*/
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
			
			
			//iAttachments
			/*this.addNewFeature("iAttachments="+deepSentence.getiAttachments(deepId));
			line+=" "+featureTranslation.get("iAttachments="+deepSentence.getiAttachments(deepId));*/
			
			
			String headId=deepSentence.getHead(deepId);
			if (headId!=null){
				if (headId.equals("0")){
					this.addNewFeature("iAttachmentsH=root");
					line+=" "+featureTranslation.get("iAttachmentsH=root");
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
							/*String sibiAttachments=deepSentence.getiAttachments(sib);						
							this.addNewFeature("sibiAttachments="+sibiAttachments);
							line+=" "+featureTranslation.get("sibiAttachments="+sibiAttachments);*/

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
		return this.featureTranslation.toString() + iAttachmentTranslation.toString();
	
	}

}
