program function_definition;

var 
    result : integer;

function Sum(a, b, c : integer): integer;
begin
    Result := a + b + c
end;

function GetMessage: string;
begin
    Result := 'Hello from GetMessage';
end;

function SumByRef(a, b, c : integer; var m : integer): integer;
begin
    m := a + b + c;
    Result := m;
end;

begin
    writeln(Sum(67, 45, 15, FavCharacter));
    writeln(GetMessage());

    result := 0;
    SumByRef(10, 20, 45, result);
    writeln(result);
end.