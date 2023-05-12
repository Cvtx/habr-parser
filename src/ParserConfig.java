/**
 * Used to alter parser work with pre-defined parameters.
 */
public class ParserConfig {

    public boolean isSilent() { return _silent; }

    public void setSilent(boolean _silent) { this._silent = _silent; }

    public String getLanguage(){ return _language; }

    public void setLanguage(String language) {
        _language = language;
    }

    public int getHaltTime() { return _haltTime; }

    public void setHaltTime(int haltTimeMilliseconds) { _haltTime = haltTimeMilliseconds; }

    private String _language = "en";
    private int _haltTime = 5000;
    private boolean _silent = false;
}
