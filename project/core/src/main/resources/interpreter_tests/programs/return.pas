program return;

function Sum(a, b, c : integer): integer;
begin
    result := a + b + c;
    Exit;
    result := 10000;
end;

begin
    writeln(Sum(67, 45, 15));
end.