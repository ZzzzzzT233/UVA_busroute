package edu.virginia.cs.hw6;

import java.io.IOException;
import java.util.List;

public interface BusLineReader {
    public List<BusLine> getBusLines() throws IOException;
}
