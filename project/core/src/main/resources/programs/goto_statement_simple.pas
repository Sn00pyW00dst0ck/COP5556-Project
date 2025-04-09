program goto_statement_simple;

label 1;
var num : integer;

begin
    num := 10;
    1 : if (num > 0) then
    begin
        num := num - 1;
        goto 1;
    end
    else
        writeln (num);
end.