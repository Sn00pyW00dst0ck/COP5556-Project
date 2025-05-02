program case_statement;

var
    number : Integer;

begin
    number := 17;
    Case number mod 2 of
        0 : writeln(number, ' mod 2 = 0');
        1 : writeln(number, ' mod 2 = 1')
    end;

    Case number of
        0, 17 : writeln(number, ' is either 17 or 0')
    end;
end.