package edu.virginia.cs.hw6;

import java.io.IOException;
import java.util.List;

public interface StopReader {
    List<Stop> getStops() throws IOException;
}
