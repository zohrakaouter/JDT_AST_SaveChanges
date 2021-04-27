package com.vogella.jdt.infos.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.filebuffers.*;

public class SampleHandler extends AbstractHandler {
	 private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(
				window.getShell(),
				"Infos",
				"Hello, Eclipse world");
		
		System.out.println(" in plugin");
		
		try 
        {
		//IPath path = new Path("TestVog2/src/testvog2.java");
		IPath path = new Path("TestWithErrors");
		IProject myproject = ResourcesPlugin.getWorkspace().getRoot().getProject(path.lastSegment());
		 IProgressMonitor myProgressMonitor=new NullProgressMonitor();
		 
         myproject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, myProgressMonitor);
         if (myproject.hasNature(JavaCore.NATURE_ID)) {
			    IJavaProject javaProject = JavaCore.create(myproject);
			    javaProject.open(null);
			     //javaProject.getResource().refreshLocal(IResource.DEPTH_INFINITE, null);
			    
			    for(IPackageFragment packageFrag : javaProject.getPackageFragments()){
			    	
			    	
			    	if(packageFrag.getPath().getFileExtension() == null){
 			    		
 			    		
 			    		for(IJavaElement javaEle : packageFrag.getChildren()){
 			    			
 			    			if(javaEle instanceof ICompilationUnit){
 			    				
 			    				ICompilationUnit compilUnit= JavaCore.createCompilationUnitFrom((IFile)javaEle.getCorrespondingResource());
 			    				ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
 			    			    
 			    				ASTParser parser = ASTParser.newParser(AST.JLS8);
 			    			       parser.setResolveBindings(true);
 			    			       parser.setKind(ASTParser.K_COMPILATION_UNIT);
 			    			      
 			    			       parser.setBindingsRecovery(true);
 			    			      parser.setSource(compilUnit);
 			    			    
 			    			      parser.setResolveBindings(true);
 			    			     
 			    			     
 			    			      parser.setBindingsRecovery(true);
 			    			    	
 			    			     CompilationUnit cu = (CompilationUnit) parser.createAST( null);
 			    			    IPath pathcu = cu.getJavaElement().getPath(); // unit: instance of CompilationUnit
 			    			   bufferManager.connect(pathcu, null); 
 			    			   System.out.println(" in pathcu "+pathcu.toString());
 			    			  
 			    			  
 			    			     AST ast= cu.getAST();
 			    			    ASTRewrite rewriter = ASTRewrite.create(ast);
 			    				if (compilUnit != null) {
 			    				 System.out.println("Compilation unit : "+compilUnit.getElementName());
 			    				
 			    	            IMarker[] ml =findJavaProblemMarkers(compilUnit);
 			    				System.out.println("ERRORS' Number: "+ml.length); 
 			    				for (int i = 0; i < ml.length; ++i) {
 			    					 
 			    				 System.out.println("Error found "+ml[i].toString());
 			    				 
 			    				 /** ast part **/
 			    				
 			    				int start = ml[i].getAttribute(IMarker.CHAR_START, 0);
   			    			    int end = ml[i].getAttribute(IMarker.CHAR_END, 0);
   			    			     
   			    			  NodeFinder nf = new NodeFinder(cu.getRoot(), start, end-start);
			    			  ASTNode an=nf.getCoveringNode();
			    			    
			    			   
			    			     System.out.println(" ASTNode ERROR: "+an);
			    			     
			    			     /**  end method declaration **/
			    			     /** Rename type**/
			    			     
			    			     if(an instanceof SimpleName)
			    			     {
			    			    	 System.out.println("  Retrieved ASTNode is SimpleName");
			    			    	
			    			    	 	ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(pathcu);
			    			    	 
			    			    	 	rewriter.set((SimpleName)an, ((SimpleName) an).IDENTIFIER_PROPERTY, "newid", null);
			    			    	 
			    			    	
			    			    	 	textFileBuffer.commit(null , false ); 
			    			    	 	
			    			    	 
	 
			    			     }
			    			    
			    			     /** END rename type **/
    		       			     
			    			   /*  Job job = Job.create("Saving changes", monitor -> {
			 			    			try {

			 			    		     saveChanges((ICompilationUnit) cu.getJavaElement(),monitor,rewriter,rewrite);
			 			    			}
			 			    			catch(Exception e) {}
			 			    			});
			 			    		job.schedule();
			 			    		*/
			    			    /** AST part end **/
			    			   
   			    			     
 			    				 }
 			    				bufferManager.disconnect(pathcu,LocationKind.IFILE, new NullProgressMonitor());
 			    				
 			    				}
 			    				
 			    				
 			    			}
 			    		}
 			    				 
 			    	}
			    }
			    
         }
         }
         catch (Exception e)
         {
        	 
         }
      //IFile file = workspace.getRoot().getFile(path);
      // CompilationUnit compilationUnit =(CompilationUnit) JavaCore.create(file);
   //  ICompilationUnit element= JavaCore.createCompilationUnitFrom(file);
     
     
     // assert "import java.util.List; \nimport java.util.Set;\nclass X {}\n".equals(document.get());
      
       
	        return null;
    }

    private void printProjectInfo(IProject project) throws CoreException,
            JavaModelException {
        System.out.println("Working in project " + project.getName());
        // check if we have a Java project
        if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
            IJavaProject javaProject = JavaCore.create(project);
            printPackageInfos(javaProject);
        }
    }

    private void printPackageInfos(IJavaProject javaProject)
            throws JavaModelException {
        IPackageFragment[] packages = javaProject.getPackageFragments();
        for (IPackageFragment mypackage : packages) {
            // Package fragments include all packages in the
            // classpath
            // We will only look at the package from the source
            // folder
            // K_BINARY would include also included JARS, e.g.
            // rt.jar
            if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                System.out.println("Package " + mypackage.getElementName());
                printICompilationUnitInfo(mypackage);

            }

        }
    }

    private void printICompilationUnitInfo(IPackageFragment mypackage)
            throws JavaModelException {
        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
            printCompilationUnitDetails(unit);

        }
    }

    private void printIMethods(ICompilationUnit unit) throws JavaModelException {
        IType[] allTypes = unit.getAllTypes();
        for (IType type : allTypes) {
            printIMethodDetails(type);
        }
    }

    private void printCompilationUnitDetails(ICompilationUnit unit)
            throws JavaModelException {
        System.out.println("Source file " + unit.getElementName());
        Document doc = new Document(unit.getSource());
        System.out.println("Has number of lines: " + doc.getNumberOfLines());
        printIMethods(unit);
    }

    private void printIMethodDetails(IType type) throws JavaModelException {
        IMethod[] methods = type.getMethods();
        for (IMethod method : methods) {

            System.out.println("Method name " + method.getElementName());
            System.out.println("Signature " + method.getSignature());
            System.out.println("Return Type " + method.getReturnType());

        }
    }
    public IMarker[] findJavaProblemMarkers(ICompilationUnit cu) 
		      throws CoreException {
	
		 System.out.println(" Compilation unit path : "+ cu.getPath());
		 
		      IResource javaSourceFile = cu.getUnderlyingResource();
		     
		      IMarker[] markers = 
		         javaSourceFile.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
		            true, IResource.DEPTH_INFINITE);
		     if(markers.length==0)
		      System.out.println("No error detected ");
		      return markers;

		   }
    private void createPackage(IProject project) throws JavaModelException {
        IJavaProject javaProject = JavaCore.create(project);
        IFolder folder = project.getFolder("src");
        // folder.create(true, true, null);
        IPackageFragmentRoot srcFolder = javaProject
                .getPackageFragmentRoot(folder);
        IPackageFragment fragment = srcFolder.createPackageFragment(
                project.getName(), true, null);
        System.out.println(" dkhal w khraj " +project.getName());
    }
    private void changeClasspath(IProject project) throws JavaModelException {
        IJavaProject javaProject = JavaCore.create(project);
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];

        System.arraycopy(entries, 0, newEntries, 0, entries.length);

        // add a new entry using the path to the container
        Path junitPath = new Path(
                "org.eclipse.jdt.junit.JUNIT_CONTAINER/4");
        IClasspathEntry junitEntry = JavaCore
                .newContainerEntry(junitPath);
        newEntries[entries.length] = JavaCore
                .newContainerEntry(junitEntry.getPath());
        javaProject.setRawClasspath(newEntries, null);
    }
    
    private void analyseMethods(IProject project) throws JavaModelException {
        IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
        // parse(JavaCore.create(project));
        for (IPackageFragment mypackage : packages) {
            if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                createAST(mypackage);
            }

        }
    }
    protected void saveChanges(ICompilationUnit cu, IProgressMonitor monitor, final ASTRewrite rewriter,
    ImportRewrite importRewrite) throws CoreException,JavaModelException,BadLocationException {
   
    	
    	TextEdit importEdits = importRewrite.rewriteImports(monitor);
    	
    	TextEdit edits = rewriter.rewriteAST(); 	
   importEdits.addChild(edits);
       Document document = new Document(cu.getSource());
        importEdits.apply(document);
       cu.getBuffer().setContents(document.get());

    
       cu.save(monitor,true);
        
    }
    

    private void createAST(IPackageFragment mypackage) throws JavaModelException {
        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
            // now create the AST for the ICompilationUnits
            CompilationUnit parse = parse(unit);
            MethodVisitor visitor = new MethodVisitor();
            parse.accept(visitor);

            for (MethodDeclaration method : visitor.getMethods()) {
                System.out.print("Method name: " + method.getName()
                        + " Return type: " + method.getReturnType2());
            }

        }
    }

    /**
     * Reads a ICompilationUnit and creates the AST DOM for manipulating the
     * Java source file
     *
     * @param unit
     * @return
     */

    private static CompilationUnit parse(ICompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(null); // parse
    }
    public static final class MethodDeclarationFinder extends ASTVisitor {
    	  private final List <MethodDeclaration> methods = new ArrayList <> ();

    	  public static List<MethodDeclaration> perform(ASTNode node) {
    	      MethodDeclarationFinder finder = new MethodDeclarationFinder();
    	      node.accept(finder);
    	      return finder.getMethods();
    	  }

    	  @Override
    	  public boolean visit (final MethodDeclaration method) {
    	    methods.add (method);
    	    return super.visit(method);
    	  }

    	  /**
    	   * @return an immutable list view of the methods discovered by this visitor
    	   */
    	  public List <MethodDeclaration> getMethods() {
    	    return Collections.unmodifiableList(methods);
    	  }
    	}
    
    
}

