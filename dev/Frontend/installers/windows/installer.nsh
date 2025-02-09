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
  ; Install Visual C++ Redistributable if needed
  ${If} $NeedVCRedist == "1"
    DetailPrint "Installing Visual C++ Redistributable 2015-2022..."
    File /oname=$TEMP\vc_redist.x64.exe "${NSISDIR}\Contrib\vcredist\vc_redist.x64.exe"
    ExecWait '"$TEMP\vc_redist.x64.exe" /quiet /norestart' $0
    ${If} $0 != "0"
      DetailPrint "Visual C++ Redistributable installation failed with code $0"
      Abort "Failed to install Visual C++ Redistributable. Please install it manually."
    ${EndIf}
    Delete "$TEMP\vc_redist.x64.exe"
  ${EndIf}

  ; Install Java if not present
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  StrCmp $1 "" 0 HaveJava
    DetailPrint "Installing Java..."
    File /oname=$TEMP\jdk-windows.exe "${NSISDIR}\Contrib\java\jdk-windows.exe"
    ExecWait '"$TEMP\jdk-windows.exe" /s' $0
    ${If} $0 != "0"
      DetailPrint "Java installation failed with code $0"
      Abort "Failed to install Java. Please install it manually."
    ${EndIf}
    Delete "$TEMP\jdk-windows.exe"
  HaveJava:

  ; Install SCIP
  DetailPrint "Installing SCIP..."
  File /oname=$TEMP\SCIPOptSuite-installer.exe "${NSISDIR}\Contrib\scip\SCIPOptSuite-installer.exe"
  ExecWait '"$TEMP\SCIPOptSuite-installer.exe" /S' $0
  ${If} $0 != "0"
    DetailPrint "SCIP installation failed with code $0"
    Abort "Failed to install SCIP. Please install it manually."
  ${EndIf}
  Delete "$TEMP\SCIPOptSuite-installer.exe"

  ; Add SCIP to PATH
  ${EnvVarUpdate} $0 "PATH" "A" "HKLM" "$PROGRAMFILES64\SCIPOptSuite\bin"
  DetailPrint "Added SCIP to PATH: $0"
!macroend