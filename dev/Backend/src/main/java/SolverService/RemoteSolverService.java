// package SolverService;

// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.beans.factory.annotation.Autowired;
// import java.util.concurrent.CompletableFuture;
// import Model.Solution;
// import org.springframework.context.annotation.Profile;
// @RestController
// @Profile("remoteSolver")
// public class RemoteSolverService {
    
//     private final SolverService solverService;
    
//     @Autowired
//     public RemoteSolverService(SolverService solverService) {
//         this.solverService = solverService;
//     }
    
//     @PostMapping("/solve")
//     public Solution solve(@RequestParam("fileId") String fileId, 
//                         @RequestParam("timeout") int timeout, 
//                         @RequestParam("solverScript") String solverScript) throws Exception {
//         return solverService.solve(fileId, timeout, solverScript);
//     }
    
//     @PostMapping("/solve-async")
//     public CompletableFuture<Solution> solveAsync(@RequestParam("fileId") String fileId, 
//                                                 @RequestParam("timeout") int timeout, 
//                                                 @RequestParam("solverScript") String solverScript) throws Exception {
//         return solverService.solveAsync(fileId, timeout, solverScript);
//     }
    
//     @PostMapping("/compile-check")
//     public String isCompiling(@RequestParam("fileId") String fileId, 
//                             @RequestParam("timeout") int timeout) throws Exception {
//         return solverService.isCompiling(fileId, timeout);
//     }
    
//     @PostMapping("/compile-check-async")
//     public CompletableFuture<String> isCompilingAsync(@RequestParam("fileId") String fileId, 
//                                                     @RequestParam("timeout") int timeout) throws Exception {
//         return solverService.isCompilingAsync(fileId, timeout);
//     }
// } 