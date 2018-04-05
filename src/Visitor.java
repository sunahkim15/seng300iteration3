//package main;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.*;

//Modified ASTVisitor
/**
 * AST Visitor for visiting References & Declarations
 * count[1] is declarations
 * count[0] is references
 * count[2] is "type" of the Type (0 = OTHER, 1 = Nested, 2 = Local, 3 = Anon (all mutually exclusive))
 */
public class Visitor extends ASTVisitor{
	
	public static final int OTHER = 0;
	public static final int NESTED = 1;
	public static final int LOCAL = 2;
	public static final int ANON = 3; 
	
	public static String typeToString(int type) {
		if (type == OTHER) {
			return "OTHER";
		}
		else if (type == NESTED) {
			return "NESTED"; 
		}
		else if (type == LOCAL) {
			return "LOCAL";
		}
		else if (type == ANON) {
			return "ANON";
		}
		else {
			return "ERROR"; // should never occur
		}
	}
	
	Map<String, Integer[]> map = new HashMap<String, Integer[]>();
	
	public Map<String, Integer[]> getMap(){
		return map;
	}
	
	//Visits when there is a primitive type (int, char, ...)
	@Override
	public boolean visit(PrimitiveType node) {
		if (node.resolveBinding() != null) {
			if(!node.toString().equals("void") & node != null) {
				String key = node.resolveBinding().getQualifiedName(); 
				Integer[] count = map.get(key);
				if(count != null) 
					count[0]++;
				else
					count = new Integer[] {1,0, OTHER};
				map.put(key, count);
			}
		}
		return super.visit(node);
	}
	
	//Visits when there is a SimpleType type (non-Primitive types like java.lang.String)
	@Override
	public boolean visit(SimpleType node) {	
		if (node.resolveBinding() != null) {
			if (node.resolveBinding().isParameterizedType()) { // handle parameterized type in its own visit method below
				return super.visit(node); 
			}
			String key = node.resolveBinding().getQualifiedName();
			if (key.equals(""))
				key = node.resolveBinding().getName();
			Integer[] count = map.get(key); 
			if(count != null) 
				count[0]++;
			else {
				if (node.resolveBinding().isAnonymous())
					count = new Integer[] {1,0, ANON};
				else if (node.resolveBinding().isLocal())
					count = new Integer[] {1,0, LOCAL};
				else if (node.resolveBinding().isMember())
					count = new Integer[] {1,0, NESTED};
				else
					count = new Integer[] {1,0, OTHER};
			}	
			map.put(key, count);
		}

		return super.visit(node);
	}
	
	//1. AnnotationType declaration
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		if (node.resolveBinding() != null) {
			String key = node.resolveBinding().getQualifiedName();
			if (key.equals(""))
				key = node.resolveBinding().getName();
			Integer[] count = map.get(key);
			if(count != null) 
				count[1]++;
			else {
				if (node.resolveBinding().isAnonymous())
					count = new Integer[] {0,1, ANON};
				else if (node.resolveBinding().isLocal())
					count = new Integer[] {0,1, LOCAL};
				else if (node.resolveBinding().isMember())
					count = new Integer[] {0,1, NESTED};
				else
					count = new Integer[] {0,1, OTHER};
			}	
			map.put(key, count);		
		}
		return super.visit(node);
	}
	
	//2. Enum declaration
	@Override
	public boolean visit(EnumDeclaration node) {
		if (node.resolveBinding() != null) {
			String key = node.resolveBinding().getQualifiedName();
			if (key.equals(""))
				key = node.resolveBinding().getName();
			Integer[] count = map.get(key);
			if(count != null) 
				count[1]++;
			else {
				if (node.resolveBinding().isAnonymous())
					count = new Integer[] {0,1, ANON};
				else if (node.resolveBinding().isLocal())
					count = new Integer[] {0,1, LOCAL};
				else if (node.resolveBinding().isMember())
					count = new Integer[] {0,1, NESTED};
				else
					count = new Integer[] {0,1, OTHER};
			}	
			map.put(key, count); 
		}
		return super.visit(node);
	}
	
	//3-4. Class / Interface declaration
	@Override
	public boolean visit(TypeDeclaration node) {
		if (node.resolveBinding() != null) {
			String key = node.resolveBinding().getQualifiedName();
			if (key.equals(""))
				key = node.resolveBinding().getName();
			Integer[] count = map.get(key);
			if(count != null) 
				count[1]++;
			else {
				if (node.resolveBinding().isAnonymous())
					count = new Integer[] {0,1, ANON};
				else if (node.resolveBinding().isLocal())
					count = new Integer[] {0,1, LOCAL};
				else if (node.resolveBinding().isMember())
					count = new Integer[] {0,1, NESTED};
				else
					count = new Integer[] {0,1, OTHER};
			}
			map.put(key, count); 
		}
		return super.visit(node);
	}
	
	// 5. Anonymous Class declaration
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		if (node.resolveBinding() != null) {
			String key = node.resolveBinding().getQualifiedName();
			if (key.equals(""))
				key = node.resolveBinding().getKey() + " (Anonymous Class)";  
			Integer[] count = map.get(key);
			if(count != null) 
				count[1]++;
			else {
				if (node.resolveBinding().isAnonymous())
					count = new Integer[] {0,1, ANON};
				else if (node.resolveBinding().isLocal())
					count = new Integer[] {0,1, LOCAL};
				else if (node.resolveBinding().isMember())
					count = new Integer[] {0,1, NESTED};
				else
					count = new Integer[] {0,1, OTHER};
			}
			map.put(key, count); 		
		}
		return super.visit(node);
	}
	
	//Import Statement
	@Override
	public boolean visit(ImportDeclaration node) {
		if (!node.isOnDemand()) { // i.e. not of the form package.*; 
			String key = node.getName().toString();  
			Integer[] count = map.get(key);
			if(count != null) 
				count[0]++;
			else
				count = new Integer[] {1,0, OTHER}; // set to default OTHER
			map.put(key, count);
		}
		return super.visit(node);
	}
	
	//Constructor declaration is a reference to its class
	@Override
	public boolean visit(MethodDeclaration node) {
		if (node.resolveBinding() != null) {
			if (node.isConstructor()) {
				String key = node.resolveBinding().getDeclaringClass().getQualifiedName();
				if (key.equals(""))
					key = node.resolveBinding().getDeclaringClass().getName(); 
				Integer[] count = map.get(key);
				if(count != null) 
					count[0]++; // increment reference count
				else {
					if (node.resolveBinding().getDeclaringClass().isAnonymous())
						count = new Integer[] {1,0, ANON};
					else if (node.resolveBinding().getDeclaringClass().isLocal())
						count = new Integer[] {1,0, LOCAL};
					else if (node.resolveBinding().getDeclaringClass().isMember())
						count = new Integer[] {1,0, NESTED};
					else
						count = new Integer[] {1,0, OTHER};
				}
				map.put(key, count); 
			}
		}
		return super.visit(node);
	}	
	
	@Override
	public boolean visit(ParameterizedType node) {
		if (node.resolveBinding() != null) {
			String key = node.resolveBinding().getTypeDeclaration().getQualifiedName(); 
			if (key.equals(""))
				key = node.resolveBinding().getTypeDeclaration().getName(); 
			Integer[] count = map.get(key);
			if(count != null) 
				count[0]++; // increment reference count
			else {
				if (node.resolveBinding().getTypeDeclaration().isAnonymous())
					count = new Integer[] {1,0, ANON};
				else if (node.resolveBinding().getTypeDeclaration().isLocal())
					count = new Integer[] {1,0, LOCAL};
				else if (node.resolveBinding().getTypeDeclaration().isMember())
					count = new Integer[] {1,0, NESTED};
				else
					count = new Integer[] {1,0, OTHER};
			}	
			map.put(key, count); 
		}
		return super.visit(node);
	}	

	@Override
	public boolean visit(ArrayType node) {	
		if (node.resolveBinding() != null) {
			String key; 
			if (node.resolveBinding().getElementType().isLocal()) {
				key = node.resolveBinding().getElementType().getName(); // name without brackets
			}
			else if (node.resolveBinding().getElementType().isParameterizedType()) {
				key = node.resolveBinding().getElementType().getTypeDeclaration().getQualifiedName(); // name without brackets
				if (key.equals(""))
					key = node.resolveBinding().getElementType().getTypeDeclaration().getName(); // name without brackets 
			}
			else {
				key = node.resolveBinding().getElementType().getQualifiedName(); // name without brackets 
				if (key.equals(""))
					key = node.resolveBinding().getElementType().getName(); // name without brackets 
			}
			int dimensions = node.getDimensions(); 
			for (int i = 0; i < dimensions; i++) {
				key += "[]";
				Integer[] count = map.get(key);
				if(count != null) 
					count[0]++; // increment reference count
				else
					count = new Integer[] {1,0, OTHER};
				map.put(key, count); 
			}
		}
		return super.visit(node);
	}
	
}