/**
 * Copyright (c) 2007-2013, National Documentation Centre (EKT, www.ekt.gr)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *     Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *     Neither the name of the National Documentation Centre nor the
 *     names of its contributors may be used to endorse or promote
 *     products derived from this software without specific prior written
 *     permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gr.ekt.oaicatbte;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.basic.BasicSplitPaneUI.KeyboardEndHandler;


import ORG.oclc.oai.server.verb.BadArgumentException;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import ORG.oclc.oai.server.verb.NoItemsMatchException;
import ORG.oclc.oai.server.verb.NoSetHierarchyException;
import ORG.oclc.oai.server.verb.OAIInternalServerError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
extends TestCase
{
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public AppTest( String testName )
	{
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( AppTest.class );
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp()
	{
		BTECatalog catalog = new BTECatalog() {

			@Override
			public Map<String, String> listAllSets() throws NoSetHierarchyException,
			OAIInternalServerError {
				// TODO Auto-generated method stub
				return null;
			}
		};

		HashMap<String, String> xsls = new HashMap<String, String>();
		xsls.put("A_B", "xsltAtoB");
		xsls.put("D_B", "xsltDtoB");
		xsls.put("B_E", "xsltBtoE");
		xsls.put("Z_H", "xsltZtoH");
		xsls.put("R1_A", "xsltR1toA");
		xsls.put("R2a_A", "xsltR2atoA");
		xsls.put("R2_R2a", "xsltR2toR2a");
		xsls.put("R3_D", "xsltR3toD");
		
		HashMap<String, List<String>> mytree = new HashMap<String, List<String>>();
		mytree.put("E", Arrays.asList(new String[]{"B"}));
		mytree.put("B", Arrays.asList(new String[]{"A","D"}));
		mytree.put("C", Arrays.asList(new String[]{"A"}));
		mytree.put("H", Arrays.asList(new String[]{"Z"}));
		mytree.put("A", Arrays.asList(new String[]{"R1","R2a"}));
		mytree.put("R2a", Arrays.asList(new String[]{"R2"}));
		mytree.put("D", Arrays.asList(new String[]{"R3"}));

		/*ArrayList<String> tmp = new ArrayList<String>();
		tmp.add("E");
		getChildsInGraph("E", null, mytree, tmp);
		System.out.println();
		 */
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		
		String[] path = new String[1000];
		for (Map.Entry<String, List<String>> tmp : mytree.entrySet()){
			if (tmp.getKey().equals("E")){
				printPaths(tmp, path, 0, mytree, xsls, null, result); 
				break;
			}
		}

		System.out.println();

		/*DSpaceBTEOAICatalog catalog = new DSpaceBTEOAICatalog(null);
		try {
			catalog.listRecords(null, null, null, "oai_dc");
			catalog.listRecords(null, null, "voa3r", "oai_dc");
			catalog.listRecords(null, null, null, "oai_dc");
			catalog.listSets();
			//Crosswalks crosswalks = catalog.getCrosswalks();
			//System.out.println("test");
		} catch (BadArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotDisseminateFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoItemsMatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSetHierarchyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAIInternalServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		assertTrue( true );
	}

	
	private void printPaths(Map.Entry<String, List<String>> node, String[] path, int pathLen, HashMap<String, List<String>> maps, HashMap<String, String> xsls, String leaf, HashMap<String, ArrayList<String>> result) { 

		if (node==null) {
			printArray(path, pathLen, leaf);
			
			int i; 
			ArrayList<String> tmp = new ArrayList<String>();
			for (i=0; i<pathLen; i++) { 
				tmp.add(path[i]); 
			} 
			tmp.add(leaf);
			
			ArrayList<String> tmp2 = new ArrayList<String>();
			for (int j=tmp.size()-1; j>0; j--){
				String from = tmp.get(j);
				String to = tmp.get(j-1);
				String xsl = xsls.get(from+"_"+to);
				
				tmp2.add(xsl);
			}
			
			
			result.put(leaf, tmp2);
			
			return;
		}

		path[pathLen] = node.getKey(); 
		pathLen++;

		for (String s : node.getValue()){
			boolean found = false;
			for (Map.Entry<String, List<String>> tmp : maps.entrySet()){
				if (tmp.getKey().equals(s)){
					found = true;
					printPaths(tmp, path, pathLen, maps, xsls, null, result); 
					break;
				}
			}
			if (!found){
				printPaths(null, path, pathLen, maps, xsls, s, result); 
			}
		} 
	}

	private void printArray(String[] ints, int len, String leaf) { 
		int i; 
		for (i=0; i<len; i++) { 
			System.out.print(ints[i] + "_"); 
		} 
		System.out.println(leaf); 
	}

}
