// package SolverService;

// import org.springframework.cloud.openfeign.FeignClient;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestParam;
// import java.util.concurrent.CompletableFuture;
// import Model.Solution;
// import org.springframework.stereotype.Component;
// import org.springframework.context.annotation.Profile;

// public interface RemoteSolverClient extends Solver {    

//     @PostMapping("/solve")
//     Solution solve(@RequestParam("fileId") String fileId, 
//                   @RequestParam("timeout") int timeout, 
//                   @RequestParam("solverScript") String solverScript) throws Exception;
    
//     @PostMapping("/solve-async")
//     CompletableFuture<Solution> solveAsync(@RequestParam("fileId") String fileId, 
//                                          @RequestParam("timeout") int timeout, 
//                                          @RequestParam("solverScript") String solverScript) throws Exception;
    
//     @PostMapping("/compile-check")
//     String isCompiling(@RequestParam("fileId") String fileId, 
//                       @RequestParam("timeout") int timeout) throws Exception;
    
//     @PostMapping("/compile-check-async")
//     CompletableFuture<String> isCompilingAsync(@RequestParam("fileId") String fileId, 
//                                               @RequestParam("timeout") int timeout) throws Exception;

//      static RemoteSolverClient create(String baseUrl) {
//         // Here you'd implement the client with a tool like RestTemplate or WebClient
//         // This is a placeholder for the actual implementation
//         return new RemoteSolverClient() {
//             // Implement the required methods here
//         };
//     }
// } 