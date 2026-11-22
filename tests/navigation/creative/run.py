"""Run the creative catalog fixture using the source release's standard NeoForge launcher."""
from importlib.util import module_from_spec, spec_from_file_location
from pathlib import Path
import sys

here = Path(__file__).resolve().parent
driver = here.parents[1] / "mod-fusion/run_native.py"
spec = spec_from_file_location("arsenal_native", driver)
native = module_from_spec(spec)
spec.loader.exec_module(native)
native.SUITES = {"creative": ("WarframeCreativeTabCheck.java", "creative_tab_check", "CREATIVE TAB COMPLETE")}
sys.argv.extend(["--fixtures", str(here)])
native.main()
