package de.codecentric.mule;

public enum OperatingSystem {
	WINDOWS_X86 {
		@Override
		String getWrapperExecutable() {
			return "wrapper-windows-x86-64.exe";
		}

		@Override
		String getMavenExecutable() {
			return "mvn.cmd";
		}

		@Override
		String getKillCommand() {
			return "taskkill /F /IM " + getWrapperExecutable();
		}
	},
	LINUX_X86 {
		@Override
		String getWrapperExecutable() {
			return "wrapper-linux-x86-64";
		}

		@Override
		String getMavenExecutable() {
			return "mvn";
		}

		@Override
		String getKillCommand() {
			return "pkill -9 " + getWrapperExecutable();
		}
	};
	
	abstract String getWrapperExecutable();
	abstract String getMavenExecutable();
	abstract String getKillCommand();
	
	public static OperatingSystem determineOperatingSystem() {
		String name = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch");
		
		if (name.indexOf("windows") > -1 && arch.indexOf("64") > - 1) {
			return WINDOWS_X86;
		} else if (name.indexOf("linux") > -1 && arch.indexOf("64") > - 1) {
			return LINUX_X86;
		} else {
			throw new IllegalStateException("Don't know operating system " + name + " / " + arch);
		}
	}
}
