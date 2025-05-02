Program Enumerations;

type
	TColors = (RED, GREEN, BLUE);
	TDay = (Mon, Tue, Wed, Thu, Fri, Sat, Sun);

var
	col : TColors;

begin
	col := GREEN;
	writeln (col);
	writeln (Sat);
end.