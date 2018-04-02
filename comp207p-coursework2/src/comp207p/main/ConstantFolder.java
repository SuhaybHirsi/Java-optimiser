package comp207p.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.util.InstructionFinder.CodeConstraint;


public class ConstantFolder {
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ConstantFolder(String classFilePath) {
		try {
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void optimize() {
		Method[] methods = gen.getMethods();
		ConstantPoolGen cpgen = gen.getConstantPool();
		// Implement your optimization here
		for (int i = 0; i < methods.length; i++) {
			if (!(methods[i].isAbstract() || methods[i].isNative())) {
				// Initialise a method generator with the original method as the baseline
				MethodGen methodGen = new MethodGen(methods[i], original.getClassName(), cpgen);
				optimiseMethod(gen, cpgen, methods[i], methodGen);
				methods[i] = methodGen.getMethod();
			}
		}

		gen.setMethods(methods);
		//	cleanConstantPool();
		gen.setMajor(50); // produce Java6 classes
		this.optimized = gen.getJavaClass();

	}

	private void optimiseMethod(ClassGen cgen, ConstantPoolGen cpgen, Method method, MethodGen methodGen) {

		Code methodCode = method.getCode(); // get code method code should be bytecode


		InstructionList instruction_List = new InstructionList(methodCode.getCode());


		//run till exhausted
		while (optimiseArithmeticOperation(methodGen) | optimiseComparisons(instruction_List, cpgen)| optimiseArithmeticOperation(methodGen))
			;

		// setPositions(true) checks whether jump handles
		// are all within the current method
		instruction_List.setPositions(true);

		// set max stack/local
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// generate the new method with optimised instructions
		Method newMethod = methodGen.getMethod();

		// replace the method in the original class
		cgen.replaceMethod(method, newMethod);
	}

	public void write(String optimisedFilePath) {
		this.optimize();

		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}


	private boolean optimiseArithmeticOperation(MethodGen mg) {
		InstructionList listOfinstructions = mg.getInstructionList();
		ConstantPoolGen cpgen = mg.getConstantPool();
		String loadInstructions = "(ConstantPushInstruction|LDC|LDC_W|LDC2_W)";// LoadInstruction   inlcude this for constantfolding and dynimic i think
		String binaryOperator = "(DADD|DDIV|DMUL|DREM|DSUB|FADD|FDIV|FMUL|FREM|FSUB|IADD|IAND|IDIV|IMUL|IOR|IREM|ISHL|ISHR|ISUB|IUSHR|IXOR|LADD|LAND|LDIV|LMUL|LOR|LREM|LSHL|LSHR|LSUB|LUSHR|LXOR|DCMPG|DCMPL|FCMPG|FCMPL|LCMP)";
		String unaryOperator = "(DNEG|FNEG|INEG|LNEG|I2B|I2C|I2D|I2F|I2L|I2S|F2D|F2I|F2L|D2F|D2I|D2L|L2D|L2F|L2I)";
		String Exp = "(" + loadInstructions + " " + loadInstructions + " " + "ArithmeticInstruction" + ")|("
				+ loadInstructions + " " + "ConversionInstruction" + ")";
		InstructionFinder f = new InstructionFinder(listOfinstructions);

		boolean modified = false;
		boolean conversion = false;
//		String
		for (Iterator e = f.search(Exp); e.hasNext(); ) { // Iterate through instructions to look for arithmetic optimisation
			InstructionHandle[] match = (InstructionHandle[]) e.next();

			InstructionHandle operationInstruction = match[match.length - 1];


			modified = true;
			InstructionHandle leftInstruction = match[0];
			InstructionHandle rightInstruction = match[1];


			try {

				if (operationInstruction.getInstruction() instanceof IADD) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "IADD");
				} else if (operationInstruction.getInstruction() instanceof LADD) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "LADD");
				} else if (operationInstruction.getInstruction() instanceof FADD) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "FADD");
				} else if (operationInstruction.getInstruction() instanceof DADD) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "DADD");
				} else if (operationInstruction.getInstruction() instanceof IMUL) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "IMUL");
				} else if (operationInstruction.getInstruction() instanceof LMUL) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "LMUL");
				} else if (operationInstruction.getInstruction() instanceof FMUL) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "FMUL");
				} else if (operationInstruction.getInstruction() instanceof DMUL) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "DMUL");
				} else if (operationInstruction.getInstruction() instanceof ISUB) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "ISUB");
				} else if (operationInstruction.getInstruction() instanceof FSUB) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "FSUB");
				} else if (operationInstruction.getInstruction() instanceof DSUB) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "DSUB");
				} else if (operationInstruction.getInstruction() instanceof IDIV) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "IDIV");
				} else if (operationInstruction.getInstruction() instanceof LDIV) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "LDIV");
				} else if (operationInstruction.getInstruction() instanceof FDIV) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "FDIV");
				} else if (operationInstruction.getInstruction() instanceof DDIV) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "DDIV");
				}
				else if(operationInstruction.getInstruction() instanceof I2F || operationInstruction.getInstruction() instanceof D2F || operationInstruction.getInstruction() instanceof L2F){ foldOperation(cpgen,listOfinstructions,match,"convert to float"); }
				else if(operationInstruction.getInstruction() instanceof I2D || operationInstruction.getInstruction() instanceof F2D|| operationInstruction.getInstruction() instanceof L2D){ foldOperation(cpgen,listOfinstructions,match,"convert to double"); }
				else if(operationInstruction.getInstruction() instanceof I2S){ foldOperation(cpgen,listOfinstructions,match,"convert to short"); }
				else if(operationInstruction.getInstruction() instanceof I2L || operationInstruction.getInstruction() instanceof F2L || operationInstruction.getInstruction() instanceof D2L){ foldOperation(cpgen,listOfinstructions,match,"convert to long"); }
				else if(operationInstruction.getInstruction() instanceof L2I || operationInstruction.getInstruction() instanceof F2I || operationInstruction.getInstruction() instanceof D2I){ foldOperation(cpgen,listOfinstructions,match,"convert to integer"); }
				else if(operationInstruction.getInstruction() instanceof I2B){ foldOperation(cpgen,listOfinstructions,match,"convert to byte"); }
				else if (operationInstruction.getInstruction() instanceof I2C) { foldOperation(cpgen,listOfinstructions,match, "convert to char"); }
				else if (operationInstruction.getInstruction() instanceof DCMPG) {
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "compare greater than doubles");
				} else if (operationInstruction.getInstruction() instanceof DCMPL)
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "compare less than doubles");
				else if (operationInstruction.getInstruction() instanceof FCMPG)
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "compare greater than floats");
				else if (operationInstruction.getInstruction() instanceof FCMPL)
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "compare less than floats");
				else if (operationInstruction.getInstruction() instanceof LCMP)
					foldOperation(leftInstruction, rightInstruction, operationInstruction, cpgen, listOfinstructions, match, "compare longs");


			} catch (ArithmeticException matherror) {
				modified = false;
			}
			break;

		}


		return modified;
	}

	private void updateLostTargets(InstructionHandle[] targets, InstructionHandle new_target) {
		for (int i = 0; i < targets.length; i++) {
			InstructionTargeter[] targeters = targets[i].getTargeters();

			for (int j = 0; j < targeters.length; j++)
				targeters[j].updateTarget(targets[i], new_target);
		}
	}

	private void foldOperation(ConstantPoolGen cpgen, InstructionList listOfInstructions, InstructionHandle[] match, String str) {
		InstructionHandle newInstruction = null;
		try {
			if (str.equals("convert to float")) {
				float number = getConstantValue(match[0], cpgen).floatValue();
				newInstruction = listOfInstructions.append(match[1], new PUSH(cpgen, number));
				listOfInstructions.delete(match[0], match[match.length - 1]);
			} else if (str.equals("convert to double")) {
				double number = getConstantValue(match[0], cpgen).doubleValue();
				newInstruction = listOfInstructions.append(match[1], new PUSH(cpgen, number));
				listOfInstructions.delete(match[0], match[match.length - 1]);
			} else if (str.equals("convert to integer")) {
				int number = getConstantValue(match[0], cpgen).intValue();
				newInstruction = listOfInstructions.append(match[1], new PUSH(cpgen, number));
				listOfInstructions.delete(match[0], match[match.length - 1]);
			} else if (str.equals("convert to short")) {
				short number = getConstantValue(match[0], cpgen).shortValue();
				newInstruction = listOfInstructions.append(match[1], new PUSH(cpgen, number));
				listOfInstructions.delete(match[0], match[match.length - 1]);
			} else if (str.equals("convert to long")) {
				long number = getConstantValue(match[0], cpgen).longValue();
				newInstruction = listOfInstructions.append(match[1], new PUSH(cpgen, number));
				listOfInstructions.delete(match[0], match[match.length - 1]);
			} else if (str.equals("convert to byte")) {
				byte number = getConstantValue(match[0], cpgen).byteValue();
				newInstruction = listOfInstructions.append(match[1], new PUSH(cpgen, number));
				listOfInstructions.delete(match[0], match[match.length - 1]);
			} else if (str.equals("convert to char")) {
				char string = (char) getConstantValue(match[0], cpgen).intValue();
				newInstruction = listOfInstructions.append(match[1], new PUSH(cpgen, string));
				listOfInstructions.delete(match[0], match[match.length - 1]);
			}

		} catch (TargetLostException ex) {
			updateLostTargets(ex.getTargets(), newInstruction);
		}

	}

	private void foldOperation(InstructionHandle leftInstruction, InstructionHandle rightInstruction, InstructionHandle operationInstruction, ConstantPoolGen cpgen, InstructionList listOfInstructions, InstructionHandle[] match, String str) {
		InstructionHandle newIns = null;
		Number leftValue;
		Number rightValue;

		rightValue = getConstantValue(rightInstruction, cpgen);
		leftValue = getConstantValue(leftInstruction, cpgen);

//		if (leftInstruction.getInstruction() instanceof ILOAD) {     // include this to make dynamics work but constantfolding breaks if u include this , it wasnot really working before as well
//
//			leftValue = getLoadInstructionValue(leftInstruction, cpgen, listOfInstructions);
//		} else {
//			leftValue = getConstantValue(leftInstruction, cpgen);
//		}
//		if (rightInstruction.getInstruction() instanceof ILOAD) {
//			rightValue = getLoadInstructionValue(rightInstruction, cpgen, listOfInstructions);
//		} else {
//			rightValue = getConstantValue(rightInstruction, cpgen);
//		}

		try {
			if (str.equals("IADD")) {

				Number sum = leftValue.intValue() + rightValue.intValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("LADD")) {
				Number sum = leftValue.longValue() + rightValue.longValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("FADD")) {
				Number sum = leftValue.floatValue() + rightValue.floatValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);
			}
			if (str.equals("DADD")) {
				Number sum = leftValue.doubleValue() + rightValue.doubleValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("IMUL")) {
				Number sum = leftValue.intValue() * rightValue.intValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("LMUL")) {
				Number sum = leftValue.longValue() * rightValue.longValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("FMUL")) {
				Number sum = leftValue.floatValue() * rightValue.floatValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("DMUL")) {
				Number sum = leftValue.doubleValue() * rightValue.doubleValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("ISUB")) {
				Number sum = leftValue.intValue() - rightValue.intValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("LSUB")) {
				Number sum = leftValue.longValue() - rightValue.longValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("FSUB")) {
				Number sum = leftValue.floatValue() - rightValue.floatValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("DSUB")) {
				Number sum = leftValue.doubleValue() - rightValue.doubleValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("IDIV")) {
				Number sum = leftValue.intValue() / rightValue.intValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("LDIV")) {
				Number sum = leftValue.longValue() / rightValue.longValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("FDIV")) {
				Number sum = leftValue.floatValue() / rightValue.floatValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			}
			if (str.equals("DDIV")) {
				Number sum = leftValue.doubleValue() / rightValue.doubleValue();
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, sum));
				listOfInstructions.delete(match[0], match[2]);

			} else if (str.equals("compare greater than doubles")) {
				int value = leftValue.doubleValue() > rightValue.doubleValue() ? 1 : -1;
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, value));
				listOfInstructions.delete(match[0], match[2]);

			} else if (str.equals("compare less than doubles")) {
				int value = leftValue.doubleValue() < rightValue.doubleValue() ? -1 : 1;
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, value));
				listOfInstructions.delete(match[0], match[2]);

			} else if (str.equals("compare greater than floats")) {
				int value = leftValue.floatValue() > rightValue.floatValue() ? 1 : -1;
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, value));
				listOfInstructions.delete(match[0], match[2]);

			} else if (str.equals("compare less than floats")) {
				int value = leftValue.floatValue() < rightValue.floatValue() ? -1 : 1;
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, value));
				listOfInstructions.delete(match[0], match[2]);

			} else if (str.equals("compare longs")) {
				int value = leftValue.longValue() > rightValue.longValue() ? 1 : (leftValue.longValue() == rightValue.longValue() ? 0 : -1);
				newIns = listOfInstructions.append(match[2], new PUSH(cpgen, value));
				listOfInstructions.delete(match[0], match[2]);

			}


		} catch (ArithmeticException ex1) {
		} catch (TargetLostException error) {
			updateLostTargets(error.getTargets(), newIns);
		}
	}


	private Number getConstantValue(InstructionHandle h, ConstantPoolGen cpgen) {
		if ((h.getInstruction() instanceof LDC) || (h.getInstruction() instanceof LDC_W)) {
			return (Number) (((LDC) h.getInstruction()).getValue(cpgen));
		} else if (h.getInstruction() instanceof LDC2_W) {
			return (((LDC2_W) h.getInstruction()).getValue(cpgen));
		} else if (h.getInstruction() instanceof ConstantPushInstruction) {
			return (((ConstantPushInstruction) h.getInstruction()).getValue());
		}
		return null;
	}

	public Number getLoadInstructionValue(InstructionHandle h, ConstantPoolGen cpgen, InstructionList list) throws RuntimeException {
		Instruction instruction = h.getInstruction();
		if (!(instruction instanceof LoadInstruction)) {
			throw new RuntimeException("InstructionHandle has to be of type LoadInstruction");
		}

		int localVariableIndex = ((LocalVariableInstruction) instruction).getIndex();

		InstructionHandle handleIterator = h;
		int incrementAccumulator = 0;
		while (!(instruction instanceof StoreInstruction) || ((StoreInstruction) instruction).getIndex() != localVariableIndex) {

			//If there is an IINC while scanning, we need to accumulate that on top of the ISTORE value
			if (instruction instanceof IINC) {
				IINC increment = (IINC) instruction;

				if (increment.getIndex() == localVariableIndex) {
					System.out.println("Found increment instruction");

					//If it's in a for loop, we cannot get the value
					if (checkForLoop(h, list) || checkIfCondition(h, list)) {
						throw new RuntimeException("IINC in for loop");
					}

					System.out.format("%s | Incrementing by %d | Index: %d\n\n", increment, increment.getIncrement(), increment.getIndex());
					incrementAccumulator += increment.getIncrement();
				}
			}

			handleIterator = handleIterator.getPrev();
			instruction = handleIterator.getInstruction();
		}

		//Go back previous one more additional time to fetch constant push instruction
		handleIterator = handleIterator.getPrev();
		instruction = handleIterator.getInstruction();

		Number storeValue = null;
		if (instruction instanceof ConstantPushInstruction) {
			storeValue = ((ConstantPushInstruction) instruction).getValue();
		} else if (instruction instanceof LDC) {
			storeValue = (Number) ((LDC) instruction).getValue(cpgen);
		} else if (instruction instanceof LDC2_W) {
			storeValue = ((LDC2_W) instruction).getValue(cpgen);
		}
		return storeValue;
	}

	public boolean checkIfCondition(InstructionHandle h, InstructionList list) {
		Instruction checkingInstruction = h.getInstruction();
		Instruction currentInstruction, currentSubInstruction;
		InstructionHandle handleIterator = h;
		while (handleIterator != null) {
			try {
				handleIterator = handleIterator.getPrev();
				currentInstruction = handleIterator.getInstruction();
				if (currentInstruction instanceof StoreInstruction
						&& ((StoreInstruction) currentInstruction).getIndex() == ((LoadInstruction) checkingInstruction).getIndex()) {
					InstructionHandle subIterator = handleIterator;
					while (subIterator != null) {
						subIterator = subIterator.getPrev();
						currentSubInstruction = subIterator.getInstruction();
						if (currentSubInstruction instanceof BranchInstruction) {
							if (((BranchInstruction) currentSubInstruction).getTarget().getPosition() > handleIterator.getPosition()) {
								return true;
							} else {
								return false;
							}
						}
					}
				}
			} catch (NullPointerException e) {
				break;
			}
		}

		return false;
	}

	public static boolean checkForLoop(InstructionHandle h, InstructionList list) {
		Instruction checkingInstruction = h.getInstruction();
		Instruction currentInstruction, previousInstruction, currentSubInstruction;
		InstructionHandle handleIterator = list.getStart();
		while (handleIterator != null) {
			try {
				handleIterator = handleIterator.getNext();
				currentInstruction = handleIterator.getInstruction();
				previousInstruction = handleIterator.getPrev().getInstruction();
				if (currentInstruction instanceof GotoInstruction
						&& (previousInstruction instanceof IINC
						|| previousInstruction instanceof StoreInstruction)
						&& (handleIterator.getPosition() > ((BranchInstruction) currentInstruction).getTarget().getPosition())) {
					if (((BranchInstruction) currentInstruction).getTarget().getInstruction().equals(checkingInstruction)) {
						return true;
					}
					InstructionHandle subIterator = handleIterator;
					while (subIterator != null) {
						subIterator = subIterator.getPrev();
						currentSubInstruction = subIterator.getInstruction();
						if (currentSubInstruction instanceof StoreInstruction) {
							if (((StoreInstruction) currentSubInstruction).getIndex() == ((LoadInstruction) checkingInstruction).getIndex()) {
								return true;
							}
						} else {
							if (subIterator.equals((InstructionHandle) ((BranchInstruction) handleIterator.getInstruction()).getTarget())) {
								break;
							}
						}
					}
				}
			} catch (NullPointerException e) {
				break;
			}
		}

		return false;
	}

	private boolean optimiseComparisons(InstructionList listOfinstructions, ConstantPoolGen cpgen) {


		return false;
	}

}
