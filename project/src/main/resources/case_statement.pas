program case_statement;

type
    TColor = (RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET);
 
var
    color : TColor;
    number : Integer;

procedure ShowColour(color : TColor);
begin
    Case color of
        RED : writeln('The color is Red');
        ORANGE : writeln('The color is Orange');
        YELLOW : writeln('The color is Yellow');
        GREEN : writeln('The color is Green');
        BLUE : writeln('The color is Blue');
        INDIGO : writeln('The color is Yellow');
        VIOLET : writeln('The color is Yellow');
    else writeln('The color is Unknown!');
    end;
end;

begin
    color := GREEN;
    ShowColour(color);

    number := 17;
    Case number mod 2 of
        0 : writeln(number, ' mod 2 = 0');
        1 : writeln(number, ' mod 2 = 1')
    end;

    Case number of
        0, 17 : writeln(number, ' is either 17 or 0');
    end;
end.
