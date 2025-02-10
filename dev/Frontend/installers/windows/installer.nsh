
!macro customInit
  ; Variables for storing return values
  Var /GLOBAL VCRedistInstalled
  Var /GLOBAL NeedVCRedist
  
  
  ; Check if Visual C++ Redistributable 2015-2022 is installed
  ReadRegDword $VCRedistInstalled HKLM "SOFTWARE\Microsoft\VisualStudio\14.0\VC\Runtimes\x64" "Installed"
  ${If} $VCRedistInstalled != "1"
    StrCpy $NeedVCRedist "1"
  ${Else}
    StrCpy $NeedVCRedist "0"
  ${EndIf}
!macroend

!macro customInstall
  
  SetOutPath $TEMP

  ; Install Visual C++ Redistributable if needed
  ${If} $NeedVCRedist == "1"
    DetailPrint "Installing Visual C++ Redistributable 2015-2022..."
    File "${PROJECT_DIR}\installers\windows\vc_redist.x64.exe"
    ExecWait '"${PROJECT_DIR}\installers\windows\vc_redist.x64.exe" /norestart' $0
    ${If} $0 != "0"
      DetailPrint "Visual C++ Redistributable installation failed with code $0"
      Abort "Failed to install Visual C++ Redistributable. Please install it manually."
    ${EndIf}
    
  ${EndIf}

  ; Install Java if not present
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  StrCmp $1 "" 0 HaveJava
    DetailPrint "Installing Java..."
    File "${PROJECT_DIR}\installers\windows\jdk-windows.exe"
    ExecWait '"${PROJECT_DIR}\installers\windows\jdk-windows.exe"' $0
    ${If} $0 != "0"
      DetailPrint "Java installation failed with code $0"
      Abort "Failed to install Java. Please install it manually."
    ${EndIf}
    
  HaveJava:

  ; Install SCIP
  DetailPrint "Installing SCIP..."
  File "${PROJECT_DIR}\installers\windows\SCIPOptSuite-installer.exe"
  ExecWait '"${PROJECT_DIR}\installers\windows\SCIPOptSuite-installer.exe"' $0
  ${If} $0 != "0"
    DetailPrint "SCIP installation failed with code $0"
    Abort "Failed to install SCIP. Please install it manually."
  ${EndIf}
  
  
!macroend