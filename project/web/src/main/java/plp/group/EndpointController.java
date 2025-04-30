package plp.group;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import plp.group.AST.AST;
import plp.group.AST.ASTBuilder;
import plp.group.Compiler.CompilerContext;
import plp.group.Optimizer.Optimizer;
import plp.group.project.delphi;
import plp.group.project.delphi_lexer;

@RestController
public class EndpointController {

	/**
	 * A DTO for a request to compile endpoint
	 */
	private record CompileRequest(
	    String filename,
	    String sourceCode // base64 encoded
	) {};

	/**
	 * A DTO for a response from compile endpoint
	 */
	private record CompileResponse(
	    boolean success,
	    String message,
	    String wasm // base64 encoded
	) {};

	/**
	 * A public POST endpoint that takes a CompileRequest and returns a CompileResponse.
	 * 
	 * TODO: convert this to run the compiler once that is ready, for now just invoke llvm
	 * 
	 * @param request the request that was received
	 * @return the compilation response
	 */
	@PostMapping(
		value = "/compile",
		consumes = "application/json",
		produces = "application/json"
	)
	public CompileResponse compile(@RequestBody CompileRequest request) {
		try {
			final String MAKEFILE_LOCATION = "./project/web/src/main/resources";
			final String TARGET = request.filename.replaceFirst("\\.ll$", ".wasm");

			String llvmCode = "";
			try {
        	    // Get the parse tree for the file we enter in command line.
        	    var lexer = new delphi_lexer(CharStreams.fromString(new String(Base64.getDecoder().decode(request.sourceCode))));
        	    var tokens = new CommonTokenStream(lexer);
        	    var parser = new delphi(tokens);
        	    var tree = parser.program();

        	    ASTBuilder builder = new ASTBuilder();
        	    AST.Program AST = (AST.Program) builder.visit(tree);
				llvmCode = (new CompilerContext()).compileToLLVMIR(AST);
				System.out.println(llvmCode);
        	} catch (Exception e) {
        	    e.printStackTrace();
        	}

			// Write the provided source code to a .ll file // TODO: SWAP THIS FOR RUNNING ANTLR AND THE COMPILER
			Files.write(Paths.get(MAKEFILE_LOCATION + "/" + request.filename), llvmCode.getBytes());
			
			// Run the makefile on the .ll file
			ProcessBuilder pb = new ProcessBuilder("make", TARGET);
			pb.redirectErrorStream(true);
			pb.directory(new File(MAKEFILE_LOCATION));
			Process process = pb.start();
			String output = new String(process.getInputStream().readAllBytes());
			int exitCode = process.waitFor();

			// Some file cleanup (optional but useful)
			Files.deleteIfExists(Paths.get(MAKEFILE_LOCATION + "/" + request.filename));
			Files.deleteIfExists(Paths.get(MAKEFILE_LOCATION + "/static/wasm/" + request.filename.replaceFirst("\\.ll$", ".o")));

			return switch (exitCode) {
				case 0 -> {
					byte[] wasmBytes = Files.readAllBytes(Paths.get(MAKEFILE_LOCATION + "/static/wasm/", TARGET));
					String base64Wasm = Base64.getEncoder().encodeToString(wasmBytes);

					yield new CompileResponse(
						true,
						"Compilation Success", 
						base64Wasm
					);
				}
				default -> new CompileResponse(
					false, 
					"Compilation Error:\n" + output,
					""
				);
			};
		} catch (Exception e) {
			return new CompileResponse(
				false, 
				"Internal Server Error:\n" + e.getMessage(), 
				""
			);
		}
  	}
}
